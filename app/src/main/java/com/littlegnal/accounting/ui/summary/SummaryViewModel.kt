package com.littlegnal.accounting.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.MvRxViewModel
import com.littlegnal.accounting.base.util.monthBetween
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.main.MainActivity
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryViewModel @AssistedInject constructor(
  @Assisted initialState: SummaryMvRxViewState,
  private val accountingDao: AccountingDao,
  private val applicationContext: Context
) : MvRxViewModel<SummaryMvRxViewState>(initialState) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
      initialState: SummaryMvRxViewState
    ): SummaryViewModel
  }

  @VisibleForTesting
  @SuppressLint("SimpleDateFormat")
  val yearMonthFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM")

  @VisibleForTesting
  val monthFormat: SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())

  fun initiate() {
    getSummaryChartData()
    getSummaryItemList()
  }

  @VisibleForTesting
  fun getSummaryChartData() {
    withState { state ->
      if (state.summaryChartData !is Uninitialized) return@withState

      accountingDao.getMonthTotalAmount(6)
          .toObservable()
          .map { list ->
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
                  add(Calendar.MONTH, -5)
                }
            val firstMonthCalendar = Calendar.getInstance()
                .apply { time = today.time }
            repeat(6) {
              val tempCalendar = Calendar.getInstance()
                  .apply { time = today.time }
              val monthString = monthFormat.format(tempCalendar.time)
              months.add(Pair(monthString, tempCalendar.time))
              today.add(Calendar.MONTH, 1)
            }

            var selectedIndex = -1

            if (list.isNotEmpty()) {
              val reverseTotalList = list.reversed()
              val latestMonthCalendar = reverseTotalList.last()
                  .let {
                    Calendar.getInstance()
                        .apply {
                          time = yearMonthFormat.parse(it.yearAndMonth)
                        }
                  }

              for (monthTotal in reverseTotalList) {
                val monthTotalCalendar = Calendar.getInstance()
                    .apply {
                      time = yearMonthFormat.parse(monthTotal.yearAndMonth)
                    }
                if ((monthTotalCalendar.get(Calendar.YEAR) ==
                        firstMonthCalendar.get(Calendar.YEAR) &&
                        monthTotalCalendar.get(Calendar.MONTH) >=
                        firstMonthCalendar.get(Calendar.MONTH)) ||
                    monthTotalCalendar.get(Calendar.YEAR) >
                    firstMonthCalendar.get(Calendar.YEAR)
                ) {
                  val index = monthTotalCalendar.monthBetween(firstMonthCalendar)
                  points.add(Pair(index, monthTotal.total))
                  val total: Float = monthTotal.total
                  values.add(
                      applicationContext.getString(
                          R.string.amount_format,
                          total
                      )
                  )
                }
              }

              selectedIndex = latestMonthCalendar.monthBetween(firstMonthCalendar)
            }

            SummaryChartData(
                points = points,
                months = months,
                values = values,
                selectedIndex = selectedIndex)
          }
          .subscribeOn(Schedulers.io())
          .execute {
            copy(summaryChartData = it)
          }
    }
  }

  @VisibleForTesting
  fun getSummaryItemList() {
    withState { state ->
      if (state.summaryItemList !is Uninitialized) return@withState

      accountingDao.getGroupingTagOfLatestMonthObservable()
          .toObservable()
          .map {
            createSummaryListItems(it)
          }
          .subscribeOn(Schedulers.io())
          .execute {
            copy(summaryItemList = it)
          }
    }
  }

  private fun createSummaryListItems(list: List<TagAndTotal>): List<SummaryListItem> {
    val summaryItemList: MutableList<SummaryListItem> = mutableListOf()
    return list.mapTo(summaryItemList) {
      val total: Float = it.total
      SummaryListItem(
          it.tagName,
          applicationContext.getString(R.string.amount_format, total)
      )
    }
  }

  fun switchMonth(date: Date) {
    val selectedCalendar = Calendar.getInstance()
        .apply { time = date }
    val year: Int = selectedCalendar.get(Calendar.YEAR)
    val month: Int = selectedCalendar.get(Calendar.MONTH) + 1
    accountingDao.getGroupingMonthTotalAmountObservable(
        year.toString(),
        String.format("%02d", month)
    )
    .map {
      createSummaryListItems(it)
    }
    .toObservable()
    .subscribeOn(Schedulers.io())
    .execute {
      copy(summaryItemList = it)
    }
  }

  companion object : MvRxViewModelFactory<SummaryViewModel, SummaryMvRxViewState> {
    override fun create(
      viewModelContext: ViewModelContext,
      state: SummaryMvRxViewState
    ): SummaryViewModel? {
      return (viewModelContext.activity as MainActivity).summaryViewModelFactory.create(state)
    }
  }
}
