package com.littlegnal.accounting.ui.summary

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import java.util.Date

data class SummaryChartData(
  val points: List<Pair<Int, Float>> = emptyList(),
  val months: List<Pair<String, Date>> = emptyList(),
  val values: List<String> = emptyList(),
  val selectedIndex: Int = -1
)

data class SummaryMvRxViewState(
  val summaryChartData: Async<SummaryChartData> = Uninitialized,
  val summaryItemList: Async<List<SummaryListItem>> = Uninitialized
) : MvRxState {
  val isLoading: Boolean = summaryChartData is Loading || summaryItemList is Loading
  val error: Throwable? = (summaryChartData as? Fail)?.error ?: (summaryItemList as? Fail)?.error
}
