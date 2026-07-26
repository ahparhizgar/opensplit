package com.opensplit.util

import kotlin.random.Random

expect fun currentTimeMillis(): Long

fun randomId(): String = Random.nextInt(1000000).toString()
