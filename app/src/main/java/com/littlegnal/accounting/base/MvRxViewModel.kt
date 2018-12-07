package com.littlegnal.accounting.base

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxStateStore
import com.littlegnal.accounting.BuildConfig

abstract class MvRxViewModel<S : MvRxState>(
  initialState: S,
  stateStore: MvRxStateStore<S>
) : BaseMvRxViewModel<S>(initialState, BuildConfig.DEBUG, stateStore)
