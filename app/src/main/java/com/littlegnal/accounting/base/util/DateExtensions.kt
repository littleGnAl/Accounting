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

/**
 * The month offset between current [Calendar] and given [Calendar]
 */
fun Calendar.monthBetween(cal: Calendar): Int {
  val month1 = get(Calendar.YEAR) * 12 + get(Calendar.MONTH)
  val month2 = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
  return Math.abs(month1 - month2)
}
