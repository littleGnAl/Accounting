package com.littlegnal.accounting.base

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.littlegnal.accounting.BuildConfig

abstract class MvRxViewModel<S : MvRxState>(
  initialState: S
) : BaseMvRxViewModel<S>(initialState, BuildConfig.DEBUG)
