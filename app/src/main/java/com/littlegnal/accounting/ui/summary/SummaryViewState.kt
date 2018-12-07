package com.littlegnal.accounting.ui.summary

import com.airbnb.mvrx.MvRxState
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import java.util.Date

data class SummaryChartData(
  val points: List<Pair<Int, Float>> = emptyList(),
  val months: List<Pair<String, Date>> = emptyList(),
  val values: List<String> = emptyList(),
  val selectedIndex: Int = -1
)

data class SummaryMvRxViewState(
  val isLoading: Boolean = false,
  val summaryChartData: SummaryChartData = SummaryChartData(),
  val summaryItemList: List<SummaryListItem> = emptyList(),
  val error: Throwable? = null
) : MvRxState
