package com.littlegnal.accounting.ui.summary

import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.MonthTotal
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import io.reactivex.Maybe
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author littlegnal
 * @date 2017/10/24
 */
class SummaryPresenterTest {

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var summaryPresenter: SummaryPresenter

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    summaryPresenter = SummaryPresenter(context, accountingDao)
  }

  @Test
  fun test_loadDataIntent_monthClickedIntent() {
    val months: MutableList<Pair<String, Date>> = mutableListOf()
    val points: MutableList<Pair<Int, Float>> = mutableListOf()
    val values: MutableList<String> = mutableListOf()

    val today = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.SECOND, 0)
    }
    val latestCalendar = Calendar.getInstance().apply { time = today.time }
    today.add(Calendar.MONTH, -5)
    val firstCalendar = Calendar.getInstance().apply { time = today.time }
    for (i in 0 until 6) {
      val tempCalendar = Calendar.getInstance().apply { time = today.time }
      val monthString = MONTH_FORMAT.format(tempCalendar.time)
      months.add(Pair(monthString, tempCalendar.time))
      today.add(Calendar.MONTH, 1)
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
        "¥100.00")
    val summaryItemList2 = SummaryListItem(
        tagAndTotal2.tagName,
        "¥200.00")

    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    `when`(accountingDao.getMonthTotalAmount(6)).thenReturn(Maybe.just(monthTotalList))
    `when`(accountingDao.getGroupingMonthTotalAmount(
        latestCalendar.get(Calendar.YEAR).toString(),
        ensureNum2Length(latestCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(listOf(tagAndTotal2))

    val dataState = SummaryViewState.SummaryDataViewState(
        points,
        months,
        values,
        5,
        listOf(summaryItemList2))

    val summaryViewImpl = SummaryViewTestImpl(summaryPresenter)
    summaryViewImpl.fireLoadDataIntent()
    summaryViewImpl.assertViewStateRendered(dataState)

    `when`(accountingDao.getGroupingMonthTotalAmountObservable(
        firstCalendar.get(Calendar.YEAR).toString(),
        ensureNum2Length(firstCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal1)))

    `when`(accountingDao.getGroupingMonthTotalAmountObservable(
        latestCalendar.get(Calendar.YEAR).toString(),
        ensureNum2Length(latestCalendar.get(Calendar.MONTH) + 1)))
        .thenReturn(Maybe.just(listOf(tagAndTotal2)))

    val groupingTagState1 = SummaryViewState.SummaryGroupingTagViewState(listOf(summaryItemList1))
    val groupingTagState2 = SummaryViewState.SummaryGroupingTagViewState(listOf(summaryItemList2))

    summaryViewImpl.fireMonthClickedIntent(firstCalendar.time)
    summaryViewImpl.assertViewStateRendered(dataState, groupingTagState1)

    summaryViewImpl.fireMonthClickedIntent(latestCalendar.time)
    summaryViewImpl.assertViewStateRendered(dataState, groupingTagState1, groupingTagState2)
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

    @BeforeClass
    @JvmStatic fun setUpRx() {
      RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @AfterClass
    @JvmStatic fun resetRx() {
      RxAndroidPlugins.reset()
    }
  }

}