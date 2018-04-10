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

import com.littlegnal.accounting.base.mvi.LceStatus
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import java.util.Date

sealed class SummaryResult {

  data class InitialResult(
    val status: LceStatus,
    val error: Throwable?,
    val points: List<Pair<Int, Float>>,
    val months: List<Pair<String, Date>>,
    val values: List<String>,
    val selectedIndex: Int,
    val summaryItemList: List<SummaryListItem>
  ) : SummaryResult() {
    companion object {
      fun success(
        points: List<Pair<Int, Float>>,
        months: List<Pair<String, Date>>,
        values: List<String>,
        selectedIndex: Int,
        summaryItemList: List<SummaryListItem>
      ): InitialResult = InitialResult(
          LceStatus.SUCCESS,
          null,
          points,
          months,
          values,
          selectedIndex,
          summaryItemList
      )

      fun failure(error: Throwable) = InitialResult(
          LceStatus.FAILURE,
          error,
          listOf(),
          listOf(),
          listOf(),
          0,
          listOf()
      )

      fun inFlight() = InitialResult(
          LceStatus.IN_FLIGHT,
          null,
          listOf(),
          listOf(),
          listOf(),
          0,
          listOf()
      )
    }
  }

  data class SwitchMonthResult(
    val status: LceStatus,
    val error: Throwable?,
    val summaryItemList: List<SummaryListItem>
  ) : SummaryResult() {
    companion object {
      fun success(summaryItemList: List<SummaryListItem>) =
        SwitchMonthResult(LceStatus.SUCCESS, null, summaryItemList)

      fun failure(error: Throwable) =
        SwitchMonthResult(LceStatus.FAILURE, error, listOf())

      fun inFlight() =
        SwitchMonthResult(LceStatus.IN_FLIGHT, null, listOf())
    }
  }
}