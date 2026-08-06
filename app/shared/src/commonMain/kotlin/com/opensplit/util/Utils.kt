package com.opensplit.util

import kotlin.math.round
import kotlin.random.Random

expect fun currentTimeMillis(): Long

fun randomId(): String = Random.nextInt(1000000).toString()

fun Double.formatAmount(): String {
  val roundedValue = round(this * 100) / 100.0
  val s = roundedValue.toString()
  val parts = s.split('.')
  val whole = parts[0]
  val decimal = if (parts.size > 1) parts[1].padEnd(2, '0').substring(0, 2) else "00"
  return "$whole.$decimal"
}
