package com.opensplit

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import com.github.takahirom.roborazzi.AnnotationFilter
import com.github.takahirom.roborazzi.DesktopComposePreviewTester
import com.github.takahirom.roborazzi.DesktopPreviewTestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.InternalRoborazziApi
import com.github.takahirom.roborazzi.getDesktopComposePreviewTester
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

class DelegatingPreview(
    private val delegate: ComposablePreview<AndroidPreviewInfo>,
    private val newPreviewInfo: AndroidPreviewInfo,
    private val newMethodName: String,
) : ComposablePreview<AndroidPreviewInfo> by delegate {
  override val previewInfo: AndroidPreviewInfo
    get() = newPreviewInfo

  override val methodName: String
    get() = newMethodName

  @Composable
  override fun invoke() {
    val isDark = (newPreviewInfo.uiMode and 0x30) == 0x20

    val localSystemTheme = localSystemThemeReflect
    val themeValue = if (isDark) systemThemeDarkReflect else systemThemeLightReflect
    isSystemInDarkTheme()
    if (localSystemTheme != null && themeValue != null) {
      CompositionLocalProvider(localSystemTheme provides themeValue) { delegate.invoke() }
    } else {
      delegate.invoke()
    }
  }
}

@RunWith(Parameterized::class)
@OptIn(InternalRoborazziApi::class, ExperimentalRoborazziApi::class)
class PreviewScreenshotTest(
    private val testParameter: DesktopPreviewTestParameter,
) {
  private val tester =
      getDesktopComposePreviewTester(
          "com.github.takahirom.roborazzi.DefaultDesktopComposePreviewTester"
      )
  private val testLifecycleOptions =
      tester.options().testLifecycleOptions
          as DesktopComposePreviewTester.Options.JUnit4TestLifecycleOptions

  @get:Rule val rule: TestRule = testLifecycleOptions.testRuleFactory()

  @Test
  fun test() {
    tester.test(testParameter)
  }

  companion object {
    val testParameters: List<DesktopPreviewTestParameter> by lazy {
      setupDefaultOptions()
      val tester =
          getDesktopComposePreviewTester(
              "com.github.takahirom.roborazzi.DefaultDesktopComposePreviewTester"
          )
      tester.testParameters()
    }

    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun values(): List<DesktopPreviewTestParameter> {
      return testParameters.map { param ->
        val originalPreview = param.preview
        val newPreview =
            DelegatingPreview(
                delegate = originalPreview,
                newPreviewInfo = originalPreview.previewInfo,
                newMethodName = originalPreview.methodName,
            )
        DesktopPreviewTestParameter(newPreview, param.manualClockOptions)
      }
    }

    fun setupDefaultOptions() {
      DesktopComposePreviewTester.defaultOptionsFromPlugin =
          DesktopComposePreviewTester.Options(
              scanOptions =
                  DesktopComposePreviewTester.Options.ScanOptions(
                      packages = listOf("com.opensplit"),
                      includePrivatePreviews = true,
                      annotationFilter =
                          AnnotationFilter.Exclude(
                              "com.github.takahirom.roborazzi.annotations.RoboPreviewExclude",
                              "com.opensplit.ui.ExcludeScreenshotTest",
                          ),
                  )
          )
    }
  }
}

// Reflection stuff to address bug of Roborazzi
val localSystemThemeReflect: ProvidableCompositionLocal<Any>? by lazy {
  try {
    val clazz = Class.forName("androidx.compose.ui.SystemThemeKt")
    val method = clazz.getDeclaredMethod("getLocalSystemTheme")
    method.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    method.invoke(null) as ProvidableCompositionLocal<Any>
  } catch (e: Exception) {
    null
  }
}

val systemThemeLightReflect: Any? by lazy {
  try {
    val clazz = Class.forName("org.jetbrains.skiko.SystemTheme")
    clazz.enumConstants.first { it.toString() == "LIGHT" || it.toString() == "Light" }
  } catch (e: Exception) {
    null
  }
}

val systemThemeDarkReflect: Any? by lazy {
  try {
    val clazz = Class.forName("org.jetbrains.skiko.SystemTheme")
    clazz.enumConstants.first { it.toString() == "DARK" || it.toString() == "Dark" }
  } catch (e: Exception) {
    null
  }
}
