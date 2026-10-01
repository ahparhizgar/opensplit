package com.opensplit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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

enum class ScreenDevice(val sizeName: String, val size: DpSize) {
  MOBILE("mobile", DpSize(390.dp, 844.dp)),
  DESKTOP("desktop", DpSize(1280.dp, 800.dp)),
}

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
    val clazz = Class.forName("androidx.compose.ui.SystemTheme")
    clazz.enumConstants.first { it.toString() == "Light" }
  } catch (e: Exception) {
    null
  }
}

val systemThemeDarkReflect: Any? by lazy {
  try {
    val clazz = Class.forName("androidx.compose.ui.SystemTheme")
    clazz.enumConstants.first { it.toString() == "Dark" }
  } catch (e: Exception) {
    null
  }
}

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

    if (localSystemTheme != null && themeValue != null) {
      CompositionLocalProvider(localSystemTheme provides themeValue) { delegate.invoke() }
    } else {
      delegate.invoke()
    }
  }
}

@RunWith(Parameterized::class)
@OptIn(InternalRoborazziApi::class, ExperimentalRoborazziApi::class)
public class PreviewScreenshotTest(
    private val testParameter: DesktopPreviewTestParameter,
) {
  private val tester =
      getDesktopComposePreviewTester(
          "com.github.takahirom.roborazzi.DefaultDesktopComposePreviewTester"
      )
  private val testLifecycleOptions =
      tester.options().testLifecycleOptions
          as DesktopComposePreviewTester.Options.JUnit4TestLifecycleOptions

  @get:Rule public val rule: TestRule = testLifecycleOptions.testRuleFactory()

  @Test
  public fun test() {
    tester.test(testParameter)
  }

  public companion object {
    public val testParameters: List<DesktopPreviewTestParameter> by lazy {
      setupDefaultOptions()
      val tester =
          getDesktopComposePreviewTester(
              "com.github.takahirom.roborazzi.DefaultDesktopComposePreviewTester"
          )
      tester.testParameters()
    }

    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    public fun values(): List<DesktopPreviewTestParameter> {
      return testParameters.flatMap { param ->
        listOf(
            createDeviceParam(param, ScreenDevice.MOBILE),
            createDeviceParam(param, ScreenDevice.DESKTOP),
        )
      }
    }

    private fun createDeviceParam(
        param: DesktopPreviewTestParameter,
        device: ScreenDevice,
    ): DesktopPreviewTestParameter {
      val originalPreview = param.preview
      val newPreviewInfo =
          originalPreview.previewInfo.copy(
              widthDp = device.size.width.value.toInt(),
              heightDp = device.size.height.value.toInt(),
          )
      val newPreview =
          DelegatingPreview(
              delegate = originalPreview,
              newPreviewInfo = newPreviewInfo,
              newMethodName = "${originalPreview.methodName}_${device.sizeName}",
          )
      return DesktopPreviewTestParameter(newPreview, param.manualClockOptions)
    }

    public fun setupDefaultOptions() {
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
