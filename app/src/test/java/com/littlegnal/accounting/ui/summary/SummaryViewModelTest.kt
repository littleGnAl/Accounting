/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting.ui.summary

import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.schedulers.TestSchedulerProvider
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.MonthTotal
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryViewModelTest {

  @Mock
  private lateinit var applicationContext: Context

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var summaryViewModel: SummaryViewModel

  private lateinit var testObserver: TestObserver<SummaryViewState>

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    summaryViewModel = SummaryViewModel(
        SummaryActionProcessorHolder(TestSchedulerProvider(), applicationContext, accountingDao)
    )
    testObserver = summaryViewModel.states()
        .test()
  }

  @Test
  fun test_initialIntent_and_switchMonthIntent() {
    val months: MutableList<Pair<String, Date>> = mutableListOf()
    val points: MutableList<Pair<Int, Float>> = mutableListOf()
    val values: MutableList<String> = mutableListOf()

    val today = Calendar.getInstance()
        .apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val latestCalendar = Calendar.getInstance()
        .apply { time = today.time }
    today.add(Calendar.MONTH, -5)
    val firstCalendar = Calendar.getInstance()
        .apply { time = today.time }

    val tempCalendar = Calendar.getInstance()
        .apply { time = firstCalendar.time }
    for (i in 0 until 6) {
      val monthCalendar = Calendar.getInstance()
          .apply { time = tempCalendar.time }
      val monthString = MONTH_FORMAT.format(monthCalendar.time)
      months.add(Pair(monthString, monthCalendar.time))
      tempCalendar.add(Calendar.MONTH, 1)
    }

    val firstMonthTotal = MonthTotal(YEAR_MONTH_FORMAT.format(firstCalendar.time), 100.0f)
    val lastMonthTotal = MonthTotal(YEAR_MONTH_FORMAT.format(latestCalendar.time), 200.0f)
    val monthTotalList = listOf(lastMonthTotal, firstMonthTotal)
    points.add(Pair(0, firstMonthTotal.total))
    points.add(Pair(5, lastMonthTotal.total))
    values.add("¥100.00")
    values.add("¥200.00")

    val tagAndTotal1 = TagAndTotal("早餐", 100.0f)
    val tagAndTotal2 = TagAndTotal("晚餐", 200.0f)

    val summaryItemList1 = SummaryListItem(
        tagAndTotal1.tagName,
        "¥100.00"
    )
    val summaryItemList2 = SummaryListItem(
        tagAndTotal2.tagName,
        "¥200.00"
    )

    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(R.string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    `when`(accountingDao.getMonthTotalAmount(6))
        .thenReturn(Maybe.just(monthTotalList))
    `when`(
        accountingDao.getGroupingMonthTotalAmount(
            latestCalendar.get(Calendar.YEAR).toString(),
            ensureNum2Length(latestCalendar.get(Calendar.MONTH) + 1)
        )
    )
        .thenReturn(listOf(tagAndTotal2))
    `when`(
        accountingDao.getGroupingMonthTotalAmountObservable(
            firstCalendar.get(Calendar.YEAR).toString(),
            ensureNum2Length(firstCalendar.get(Calendar.MONTH) + 1)
        )
    )
        .thenReturn(Maybe.just(listOf(tagAndTotal1)))

    `when`(
        accountingDao.getGroupingMonthTotalAmountObservable(
            latestCalendar.get(Calendar.YEAR).toString(),
            ensureNum2Length(latestCalendar.get(Calendar.MONTH) + 1)
        )
    )
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    val intents = Observable.merge(
        Observable.just(SummaryIntent.InitialIntent()),
        Observable.just(SummaryIntent.SwitchMonthIntent(firstCalendar.time)),
        Observable.just(SummaryIntent.SwitchMonthIntent(latestCalendar.time))
    )
    summaryViewModel.processIntents(intents)
    testObserver.assertValueAt(
        1,
        SummaryViewState(
            true,
            null,
            listOf(),
            listOf(),
            listOf(),
            0,
            listOf(),
            false
        )
    )
    testObserver.assertValueAt(
        2,
        SummaryViewState(
            false,
            null,
            points,
            months,
            values,
            5,
            listOf(summaryItemList2),
            false
        )
    )
    testObserver.assertValueAt(
        3,
        SummaryViewState(
            true,
            null,
            points,
            months,
            values,
            5,
            listOf(summaryItemList2),
            true
        )
    )
    testObserver.assertValueAt(
        4,
        SummaryViewState(
            false,
            null,
            points,
            months,
            values,
            5,
            listOf(summaryItemList1),
            true
        )
    )
    testObserver.assertValueAt(
        5,
        SummaryViewState(
            true,
            null,
            points,
            months,
            values,
            5,
            listOf(summaryItemList1),
            true
        )
    )
    testObserver.assertValueAt(
        6,
        SummaryViewState(
            false,
            null,
            points,
            months,
            values,
            5,
            listOf(summaryItemList2),
            true
        )
    )
  }

  private fun ensureNum2Length(num: Int): String =
    if (num < 10) {
      "0$num"
    } else {
      num.toString()
    }

  companion object {
    private val YEAR_MONTH_FORMAT = SimpleDateFormat("yyyy-MM")

    private val MONTH_FORMAT = SimpleDateFormat("MMM", Locale.getDefault())
  }
}
