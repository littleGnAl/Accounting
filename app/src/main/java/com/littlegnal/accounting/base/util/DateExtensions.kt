package com.littlegnal.accounting.base.util

import java.util.Calendar
import java.util.Date

fun Date.toHms0(): Date = Calendar.getInstance().apply {
  time = this@toHms0
  set(Calendar.HOUR, 0)
  set(Calendar.MINUTE, 0)
  set(Calendar.SECOND, 0)
  set(Calendar.MILLISECOND, 0)
}.time
