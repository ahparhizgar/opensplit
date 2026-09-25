package com.opensplit.db

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

object InstantConverter {
  @ColumnTypeConverter
  fun fromEpochMilliseconds(value: Long?): Instant? {
    return value?.let { Instant.fromEpochMilliseconds(it) }
  }

  @ColumnTypeConverter
  fun toEpochMilliseconds(instant: Instant?): Long? {
    return instant?.toEpochMilliseconds()
  }
}
