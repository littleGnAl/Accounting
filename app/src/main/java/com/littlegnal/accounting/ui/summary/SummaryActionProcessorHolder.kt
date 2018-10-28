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

import android.annotation.SuppressLint
import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.mvi.MviAction
import com.littlegnal.accounting.base.mvi.MviResult
import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.TagAndTotal
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 用于处理所有[MviAction]的业务逻辑，并把[MviAction]转换成[MviResult]
 */
class SummaryActionProcessorHolder(
  private val schedulerProvider: BaseSchedulerProvider,
  private val applicationContext: Context,
  private val accountingDao: AccountingDao
) {

  @SuppressLint("SimpleDateFormat")
  private val yearMonthFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM")

  private val monthFormat: SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())

  private fun ensureNum2Length(num: Int): String =
    if (num < 10) {
      "0$num"
    } else {
      num.toString()
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

  private fun calcMonthOffset(
    calendar1: Calendar,
    calendar2: Calendar
  ): Int {
    val month1 = calendar1.get(Calendar.YEAR) * 12 + calendar1.get(Calendar.MONTH)
    val month2 = calendar2.get(Calendar.YEAR) * 12 + calendar2.get(Calendar.MONTH)
    return Math.abs(month1 - month2)
  }

  private val initialProcessor =
    ObservableTransformer<SummaryAction.InitialAction, SummaryResult.InitialResult> { actions ->
      actions.flatMap {
        // 只显示6个月的汇总数据
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
              for (i in 0 until 6) {
                val tempCalendar = Calendar.getInstance()
                    .apply { time = today.time }
                val monthString = monthFormat.format(tempCalendar.time)
                months.add(Pair(monthString, tempCalendar.time))
                today.add(Calendar.MONTH, 1)
              }

              var summaryItemList: List<SummaryListItem> = listOf()
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

                val tagAndTotalList = accountingDao.getGroupingMonthTotalAmount(
                    latestMonthCalendar.get(Calendar.YEAR).toString(),
                    ensureNum2Length(latestMonthCalendar.get(Calendar.MONTH) + 1)
                )
                summaryItemList = createSummaryListItems(tagAndTotalList)

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

              SummaryResult.InitialResult
                  .success(points, months, values, selectedIndex, summaryItemList)
            }
            .onErrorReturn {
              Timber.e(it)
              SummaryResult.InitialResult.failure(it)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(SummaryResult.InitialResult.inFlight())
      }
    }

  private val switchMonthProcessor =
    ObservableTransformer<
        SummaryAction.SwitchMonthAction,
        SummaryResult.SwitchMonthResult> { actions ->
      actions.flatMap {
        val selectedCalendar = Calendar.getInstance()
            .apply { time = it.date }
        val year: Int = selectedCalendar.get(Calendar.YEAR)
        val month: Int = selectedCalendar.get(Calendar.MONTH) + 1
        accountingDao.getGroupingMonthTotalAmountObservable(
            year.toString(),
            ensureNum2Length(month)
        )
            .toObservable()
            .map {
              SummaryResult.SwitchMonthResult.success(createSummaryListItems(it))
            }
            .onErrorReturn {
              Timber.e(it)
              SummaryResult.SwitchMonthResult.failure(it)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(SummaryResult.SwitchMonthResult.inFlight())
      }
    }

  /**
   * 拆分[Observable<MviAction>]并且为不同的[MviAction]提供相应的processor，processor用于处理业务逻辑，
   * 同时把[MviAction]转换为[MviResult]，最终通过[Observable.merge]合并回一个流
   *
   * 为了防止遗漏[MviAction]未处理，在流的最后合并一个错误检测，方便维护
   */
  val actionProcessor: ObservableTransformer<SummaryAction, SummaryResult> =
    ObservableTransformer { actions ->
      actions.publish { shared ->
        Observable.merge(
            shared.ofType(SummaryAction.InitialAction::class.java)
                .compose(initialProcessor),
            shared.ofType(SummaryAction.SwitchMonthAction::class.java)
                .compose(switchMonthProcessor)
        )
            .mergeWith(shared.filter {
              it !is SummaryAction.InitialAction &&
                  it !is SummaryAction.SwitchMonthAction
            }
                .flatMap {
                  Observable.error<SummaryResult>(
                      IllegalArgumentException("Unknown Action type: $it")
                  )
                })
      }
    }
}
