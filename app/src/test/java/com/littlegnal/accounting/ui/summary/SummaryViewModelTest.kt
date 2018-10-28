package com.littlegnal.accounting.ui.summary

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.littlegnal.accounting.R.string
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
import com.littlegnal.accounting.base.TestMvRxStateStore
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

  private lateinit var testMvRxStateStore: TestMvRxStateStore<SummaryMvRxViewState>

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    owner = TestLifecycleOwner()
    owner.lifecycle.markState(Lifecycle.State.STARTED)

    val initialState = SummaryMvRxViewState()
    testMvRxStateStore = TestMvRxStateStore.create(initialState)
    summaryViewModel = SummaryViewModel(
        initialState,
        testMvRxStateStore,
        accountingDao,
        applicationContext
    )
  }

  @Test
  fun loadSummary_and_switchMonth() {
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
        accountingDao.getLastGroupingMonthTotalAmountObservable())
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))
    Mockito.`when`(
        accountingDao.getGroupingMonthTotalAmountObservable(
            firstCalendar.get(Calendar.YEAR).toString(),
            summaryViewModel.ensureNum2Length(firstCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal1)))

    Mockito.`when`(accountingDao.getGroupingMonthTotalAmountObservable(
            latestCalendar.get(Calendar.YEAR).toString(),
            summaryViewModel.ensureNum2Length(latestCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    summaryViewModel.loadSummary()
    summaryViewModel.switchMonth(firstCalendar.time)
    summaryViewModel.switchMonth(latestCalendar.time)

    val state1 = SummaryMvRxViewState(
            isLoading = true,
            error = null,
            summaryChartData = SummaryChartData(),
            summaryItemList = listOf())
    val state2 = SummaryMvRxViewState(
            isLoading = false,
            error = null,
            summaryChartData = SummaryChartData(points, months, values, 5),
            summaryItemList = listOf(summaryItemList2))
    val state3 = SummaryMvRxViewState(
            isLoading = true,
            error = null,
            summaryChartData = SummaryChartData(points, months, values, 5),
            summaryItemList = listOf(summaryItemList2))
    val state4 = SummaryMvRxViewState(
            isLoading = false,
            error = null,
            summaryChartData = SummaryChartData(points, months, values, 5),
            summaryItemList = listOf(summaryItemList1))
    val state5 = SummaryMvRxViewState(
            isLoading = true,
            error = null,
            summaryChartData = SummaryChartData(points, months, values, 5),
            summaryItemList = listOf(summaryItemList1))
    val state6 = SummaryMvRxViewState(
            isLoading = false,
            error = null,
            summaryChartData = SummaryChartData(points, months, values, 5),
            summaryItemList = listOf(summaryItemList2))

    testMvRxStateStore.testAllStates { stateList ->
        stateList.size == 7 &&
            stateList[0] == SummaryMvRxViewState() &&
            stateList[1] == state1 &&
            stateList[2] == state2 &&
            stateList[3] == state3 &&
            stateList[4] == state4 &&
            stateList[5] == state5 &&
            stateList[6] == state6
    }
  }
}
