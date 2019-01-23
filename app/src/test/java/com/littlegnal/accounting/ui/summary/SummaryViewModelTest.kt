package com.littlegnal.accounting.ui.summary

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.R.string
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.MonthTotal
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.Calendar
import java.util.Date

class SummaryViewModelTest {

  @Rule
  @JvmField
  val scheduler = RxImmediateSchedulerRule()

  @Mock
  private lateinit var applicationContext: Context

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var summaryViewModel: SummaryViewModel

  private lateinit var owner: TestLifecycleOwner

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    owner = TestLifecycleOwner()
    owner.lifecycle.markState(Lifecycle.State.STARTED)

    val initialState = SummaryMvRxViewState()
    summaryViewModel = SummaryViewModel(
        initialState,
        accountingDao,
        applicationContext
    )
  }

  @Test
  fun initiate_success() {
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
      val monthString = summaryViewModel.monthFormat.format(monthCalendar.time)
      months.add(Pair(monthString, monthCalendar.time))
      tempCalendar.add(Calendar.MONTH, 1)
    }

    val firstMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(firstCalendar.time), 100.0f)
    val lastMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(latestCalendar.time), 200.0f)
    val monthTotalList = listOf(lastMonthTotal, firstMonthTotal)
    points.add(Pair(0, firstMonthTotal.total))
    points.add(Pair(5, lastMonthTotal.total))
    values.add("¥100.00")
    values.add("¥200.00")

    Mockito.`when`(applicationContext.getString(string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    Mockito.`when`(applicationContext.getString(string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    Mockito.`when`(accountingDao.getMonthTotalAmount(6))
        .thenReturn(Maybe.just(monthTotalList))

    val tagAndTotal2 = TagAndTotal("晚餐", 200.0f)
    val summaryItemList2 = SummaryListItem(
        tagAndTotal2.tagName,
        "¥200.00"
    )
    Mockito.`when`(applicationContext.getString(string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    Mockito.`when`(
        accountingDao.getGroupingTagOfLatestMonthObservable())
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    summaryViewModel.initiate()
    withState(summaryViewModel) {
      assert(it.summaryChartData is Success &&
          it.summaryChartData() == SummaryChartData(points, months, values, 5))
      assert(it.summaryItemList is Success && it.summaryItemList() == listOf(summaryItemList2))
    }
  }

  @Test
  fun getSummaryChartData_success() {
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
      val monthString = summaryViewModel.monthFormat.format(monthCalendar.time)
      months.add(Pair(monthString, monthCalendar.time))
      tempCalendar.add(Calendar.MONTH, 1)
    }

    val firstMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(firstCalendar.time), 100.0f)
    val lastMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(latestCalendar.time), 200.0f)
    val monthTotalList = listOf(lastMonthTotal, firstMonthTotal)
    points.add(Pair(0, firstMonthTotal.total))
    points.add(Pair(5, lastMonthTotal.total))
    values.add("¥100.00")
    values.add("¥200.00")

    Mockito.`when`(applicationContext.getString(string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    Mockito.`when`(applicationContext.getString(string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    Mockito.`when`(accountingDao.getMonthTotalAmount(6))
        .thenReturn(Maybe.just(monthTotalList))

    val success = SummaryChartData(points, months, values, 5)

    val data = mutableListOf<Async<SummaryChartData>>()
    summaryViewModel.selectSubscribe(owner, SummaryMvRxViewState::summaryChartData) {
      data.add(it)
    }
    summaryViewModel.getSummaryChartData()
    assert(data.size == 3 && data[1] is Loading && data[2] is Success && data[2]() == success)
  }

  @Test
  fun getSummaryItemList_success() {
    val tagAndTotal2 = TagAndTotal("晚餐", 200.0f)
    val summaryItemList2 = SummaryListItem(
        tagAndTotal2.tagName,
        "¥200.00"
    )
    Mockito.`when`(applicationContext.getString(string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    Mockito.`when`(
        accountingDao.getGroupingTagOfLatestMonthObservable())
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    val success = listOf(summaryItemList2)

    val data = mutableListOf<Async<List<SummaryListItem>>>()
    summaryViewModel.selectSubscribe(owner, SummaryMvRxViewState::summaryItemList) {
      data.add(it)
    }
    summaryViewModel.getSummaryItemList()
    assert(data.size == 3 && data[1] is Loading && data[2] is Success && data[2]() == success)
  }

  @Test
  fun switchMonth_success() {
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
      val monthString = summaryViewModel.monthFormat.format(monthCalendar.time)
      months.add(Pair(monthString, monthCalendar.time))
      tempCalendar.add(Calendar.MONTH, 1)
    }

    val firstMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(firstCalendar.time), 100.0f)
    val lastMonthTotal = MonthTotal(summaryViewModel.yearMonthFormat
        .format(latestCalendar.time), 200.0f)
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

    Mockito.`when`(applicationContext.getString(string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    Mockito.`when`(applicationContext.getString(string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    Mockito.`when`(accountingDao.getMonthTotalAmount(6))
        .thenReturn(Maybe.just(monthTotalList))
    Mockito.`when`(
        accountingDao.getGroupingTagOfLatestMonthObservable())
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))
    Mockito.`when`(
        accountingDao.getGroupingMonthTotalAmountObservable(
            firstCalendar.get(Calendar.YEAR).toString(),
            String.format("%02d", firstCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal1)))

    Mockito.`when`(accountingDao.getGroupingMonthTotalAmountObservable(
        latestCalendar.get(Calendar.YEAR).toString(),
        String.format("%02d", latestCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    summaryViewModel.getSummaryChartData()
    summaryViewModel.getSummaryItemList()

    val data1 = mutableListOf<Async<List<SummaryListItem>>>()
    summaryViewModel.selectSubscribe(owner, SummaryMvRxViewState::summaryItemList) {
      data1.add(it)
    }
    summaryViewModel.switchMonth(firstCalendar.time)
    assert(data1.size == 3 &&
        data1[1] is Loading &&
        data1[2] is Success &&
        data1[2]() == listOf(summaryItemList1))

    val data2 = mutableListOf<Async<List<SummaryListItem>>>()
    summaryViewModel.selectSubscribe(owner, SummaryMvRxViewState::summaryItemList) {
      data2.add(it)
    }
    summaryViewModel.switchMonth(latestCalendar.time)
    assert(data2.size == 3 &&
        data2[1] is Loading &&
        data2[2] is Success &&
        data2[2]() == listOf(summaryItemList2))
  }
}
