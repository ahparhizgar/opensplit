package com.opensplit.ui

/** Annotation to exclude a `@Preview` from Roborazzi screenshot tests. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeScreenshotTest
