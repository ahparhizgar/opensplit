package com.opensplit

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

class DelegatingPreview(
    private val delegate: ComposablePreview<AndroidPreviewInfo>,
    private val newPreviewInfo: AndroidPreviewInfo,
    private val newMethodName: String,
) : ComposablePreview<AndroidPreviewInfo> by delegate {
  override val previewInfo: AndroidPreviewInfo
    get() = newPreviewInfo

  override val methodName: String
    get() = newMethodName
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
              newMethodName = originalPreview.methodName,
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
