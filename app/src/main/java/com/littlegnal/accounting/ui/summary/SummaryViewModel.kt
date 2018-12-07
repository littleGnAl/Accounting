package com.littlegnal.accounting.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.MvRxViewModel
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.main.MainActivity
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryViewModel @AssistedInject constructor(
  @Assisted initialState: SummaryMvRxViewState,
  @Assisted stateStore: MvRxStateStore<SummaryMvRxViewState>,
  private val accountingDao: AccountingDao,
  private val applicationContext: Context
) : MvRxViewModel<SummaryMvRxViewState>(initialState, stateStore) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
      initialState: SummaryMvRxViewState,
      stateStore: MvRxStateStore<SummaryMvRxViewState>
    ): SummaryViewModel
  }

  @VisibleForTesting
  @SuppressLint("SimpleDateFormat")
  val yearMonthFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM")

  @VisibleForTesting
  val monthFormat: SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())

  fun loadSummary() {
    Observable.zip(
        getSummaryChartData(),
        getSummaryItemList(),
        BiFunction { summaryChartData: SummaryChartData, list: List<SummaryListItem> ->
          summaryChartData to list
        })
        .execute {
          when (it) {
            is Fail -> {
              copy(isLoading = false, error = it.error)
            }
            is Success -> {
              val pair = it()!!
              copy(
                  isLoading = false,
                  summaryChartData = pair.first,
                  summaryItemList = pair.second)
            }
            is Loading -> {
              copy(isLoading = true)
            }
            else -> {
              copy()
            }
          }
        }
  }

  private fun getSummaryChartData() =
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

//          var summaryItemList: List<SummaryListItem> = listOf()
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
                  val index = calcMonthOffset(monthTotalCalendar, firstMonthCalendar)
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

              selectedIndex = calcMonthOffset(latestMonthCalendar, firstMonthCalendar)
            }

            SummaryChartData(
                points = points,
                months = months,
                values = values,
                selectedIndex = selectedIndex)
          }
          .subscribeOn(Schedulers.io())

  private fun getSummaryItemList() =
      accountingDao.getLastGroupingMonthTotalAmountObservable()
        .toObservable()
        .map {
          createSummaryListItems(it)
        }
        .subscribeOn(Schedulers.io())

  // TODO: make extensions function
  @VisibleForTesting
  fun ensureNum2Length(num: Int): String =
      if (num < 10) {
        "0$num"
      } else {
        num.toString()
      }

  // TODO: make extensions function
  private fun calcMonthOffset(
    calendar1: Calendar,
    calendar2: Calendar
  ): Int {
    val month1 = calendar1.get(Calendar.YEAR) * 12 + calendar1.get(Calendar.MONTH)
    val month2 = calendar2.get(Calendar.YEAR) * 12 + calendar2.get(Calendar.MONTH)
    return Math.abs(month1 - month2)
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
        ensureNum2Length(month)
    )
    .map {
      createSummaryListItems(it)
    }
    .toObservable()
    .subscribeOn(Schedulers.io())
    .execute {
      when (it) {
        is Loading -> {
          copy(isLoading = true, error = null)
        }
        is Fail -> {
          copy(isLoading = false, error = it.error)
        }
        is Success -> {
          copy(isLoading = false, error = null, summaryItemList = it()!!)
        }
        else -> {
          copy()
        }
      }
    }
  }

  companion object : MvRxViewModelFactory<SummaryMvRxViewState> {
    @JvmStatic override fun create(
      activity: FragmentActivity,
      state: SummaryMvRxViewState
    ): BaseMvRxViewModel<SummaryMvRxViewState> {
      return (activity as MainActivity).summaryViewModelFactory
          .create(state, RealMvRxStateStore(state))
    }
  }
}
