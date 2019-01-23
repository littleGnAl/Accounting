package com.littlegnal.accounting.base.util

import org.junit.Test
import java.util.Calendar

class DateExtensionsTest {

  @Test
  fun monthBetween_sameYear() {
    val c1 = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2019)
      set(Calendar.MONTH, 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }

    val c2 = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2019)
      set(Calendar.MONTH, 5)
      set(Calendar.DAY_OF_MONTH, 1)
    }

    assert(c1.monthBetween(c2) == 4)
  }

  @Test
  fun monthBetween_differentYear() {
    val c1 = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2018)
      set(Calendar.MONTH, 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }

    val c2 = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2019)
      set(Calendar.MONTH, 5)
      set(Calendar.DAY_OF_MONTH, 1)
    }

    assert(c1.monthBetween(c2) == 16)
  }
}
