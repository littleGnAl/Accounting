package com.littlegnal.accounting.ui.main

import com.airbnb.mvrx.MvRxState
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import java.util.Calendar
import java.util.Date

data class MainState(
  val error: Throwable? = null,
  val isLoading: Boolean = false,
  val accountingDetailList: List<MainAccountingDetail> = emptyList(),
  val isNoData: Boolean = false,
  val isNoMoreData: Boolean = false,
  val lastDate: Date = Calendar.getInstance().time
) : MvRxState
