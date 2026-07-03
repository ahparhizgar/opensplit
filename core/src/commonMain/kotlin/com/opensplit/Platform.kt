package com.opensplit

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform
