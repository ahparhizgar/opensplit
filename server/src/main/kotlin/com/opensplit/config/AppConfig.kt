package com.opensplit.config

data class AppConfig(val isDevelopment: Boolean) {
  companion object {
    fun fromEnvironment(developmentMode: Boolean): AppConfig {
      val envFlag = System.getenv("DEVELOPMENT")?.toBoolean() ?: false
      return AppConfig(isDevelopment = developmentMode || envFlag)
    }

    fun test(): AppConfig = AppConfig(isDevelopment = false)
  }
}
