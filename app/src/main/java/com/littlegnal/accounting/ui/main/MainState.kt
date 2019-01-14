package com.littlegnal.accounting.ui.main

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import java.util.Calendar
import java.util.Date

data class MainState(
  val accountingDetailList: Async<List<MainAccountingDetail>> = Uninitialized,
  val isNoData: Boolean = false,
  val isNoMoreData: Boolean = false,
  val lastDate: Date = Calendar.getInstance().time
) : MvRxState
