package com.littlegnal.accounting.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.littlegnal.accounting.R
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.MonthTotal
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/9/26
 */
class SummaryPresenter @Inject constructor(
    private val applicationContext: Context,
    private val accountingDao: AccountingDao
) : MviBasePresenter<SummaryView, SummaryViewState>() {

  @SuppressLint("SimpleDateFormat")
  private val yearMonthFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM")

  private val monthFormat: SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())

  override fun bindIntents() {
    val summaryPeriodChangeIntent: Observable<SummaryViewState> =
      intent { it.loadDataIntent() }
          .doOnNext { Timber.d("summaryPeriodChangeIntent") }
          .flatMap {
            // 只显示6个月的汇总数据
            accountingDao.getMonthTotalAmount(6)
                .toObservable()
                .map { createDataState(it) }
                .subscribeOn(Schedulers.io())
          }

    val monthClickedIntent: Observable<SummaryViewState> =
      intent { it.monthClickedIntent() }
          .doOnNext { Timber.d("monthClickedIntent") }
          .map { Calendar.getInstance().apply { time = it } }
          .flatMap { selectedCalendar ->
            val year: Int = selectedCalendar.get(Calendar.YEAR)
            val month: Int = selectedCalendar.get(Calendar.MONTH) + 1
            accountingDao.getGroupingMonthTotalAmountObservable(
                year.toString(),
                ensureNum2Length(month))
                .toObservable()
                .map { createSummaryListItems(it) }
                .map { SummaryViewState.SummaryGroupingTagViewState(it) }
                .subscribeOn(Schedulers.io())
          }

    // 把2个intent合并为一个流
    val allIntents = Observable.merge(monthClickedIntent, summaryPeriodChangeIntent)
          .observeOn(AndroidSchedulers.mainThread())

    subscribeViewState(
        allIntents,
        SummaryView::render)
  }

  private fun createDataState(list: List<MonthTotal>): SummaryViewState {
    val months: MutableList<Pair<String, Date>> = mutableListOf()
    val points: MutableList<Pair<Int, Float>> = mutableListOf()
    val values: MutableList<String> = mutableListOf()

    val today = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      add(Calendar.MONTH, -5)
    }
    val firstMonthCalendar = Calendar.getInstance().apply { time = today.time }
    for (i in 0 until 6) {
      val tempCalendar = Calendar.getInstance().apply { time = today.time }
      val monthString = monthFormat.format(tempCalendar.time)
      months.add(Pair(monthString, tempCalendar.time))
      today.add(Calendar.MONTH, 1)
    }

    var summaryItemList: List<SummaryListItem> = listOf()
    var selectedIndex = -1

    if (list.isNotEmpty()) {
      val reverseTotalList = list.reversed()
      val latestMonthCalendar = reverseTotalList.last().let {
        Calendar.getInstance()
            .apply {
              time = yearMonthFormat.parse(it.yearAndMonth)
            }
      }

      val tagAndTotalList = accountingDao.getGroupingMonthTotalAmount(
          latestMonthCalendar.get(Calendar.YEAR).toString(),
          ensureNum2Length(latestMonthCalendar.get(Calendar.MONTH) + 1))
      summaryItemList = createSummaryListItems(tagAndTotalList)

      for (monthTotal in reverseTotalList) {
        val monthTotalCalendar = Calendar.getInstance().apply {
          time = yearMonthFormat.parse(monthTotal.yearAndMonth)
        }
        if ((monthTotalCalendar.get(Calendar.YEAR) == firstMonthCalendar.get(Calendar.YEAR) &&
            monthTotalCalendar.get(Calendar.MONTH) >= firstMonthCalendar.get(Calendar.MONTH)) ||
            monthTotalCalendar.get(Calendar.YEAR) > firstMonthCalendar.get(Calendar.YEAR)) {
          val index = calcMonthOffset(monthTotalCalendar, firstMonthCalendar)
          points.add(Pair(index, monthTotal.total))
          values.add(applicationContext.getString(
              R.string.amount_format,
              monthTotal.total))
        }
      }

      selectedIndex = calcMonthOffset(latestMonthCalendar, firstMonthCalendar)
    }

    return SummaryViewState.SummaryDataViewState(
        points,
        months,
        values,
        selectedIndex,
        summaryItemList)
  }

  private fun createSummaryListItems(list: List<TagAndTotal>): List<SummaryListItem> {
    val summaryItemList: MutableList<SummaryListItem> = mutableListOf()
    return list.mapTo(summaryItemList) {
      SummaryListItem(
          it.tagName,
          applicationContext.getString(R.string.amount_format, it.total))
    }
  }

  private fun ensureNum2Length(num: Int): String =
      if (num < 10) {
        "0$num"
      } else {
        num.toString()
      }

  private fun calcMonthOffset(calendar1: Calendar, calendar2: Calendar): Int {
    val yearOffset = Math.abs(calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR))
    val monthOffset = Math.abs(calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH))
    return yearOffset * 12 + monthOffset
  }
}