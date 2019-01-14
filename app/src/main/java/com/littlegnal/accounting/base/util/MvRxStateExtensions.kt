package com.littlegnal.accounting.base.util

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success

// TODO: Add unit test
inline fun <S : MvRxState, T, V> S.success(value: Async<T>, callBack: (T) -> V) {
  if (value is Success) {
    callBack(value())
  }
}
