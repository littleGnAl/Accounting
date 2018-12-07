package com.littlegnal.accounting.base

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.RealMvRxStateStore
import io.reactivex.Observable

class TestMvRxStateStore<S : MvRxState> private constructor(
  private val realMvRxStateStore: RealMvRxStateStore<S>
) : MvRxStateStore<S> {

  companion object {
    fun <S : MvRxState> create(
      initialState: S
    ): TestMvRxStateStore<S> = TestMvRxStateStore(RealMvRxStateStore(initialState))
  }

  override val observable: Observable<S>
    get() = realMvRxStateStore.observable

  override val state: S
    get() = realMvRxStateStore.state

  override fun dispose() {
    realMvRxStateStore.dispose()
  }

  override fun get(block: (S) -> Unit) {
    realMvRxStateStore.get(block)
  }

  override fun isDisposed(): Boolean = realMvRxStateStore.isDisposed

  override fun set(stateReducer: S.() -> S) {
    realMvRxStateStore.set(stateReducer)
  }

  private val _allStates = mutableListOf<S>()

  init {
    realMvRxStateStore.observable.subscribe {
      _allStates.add(it)
    }
  }

  fun testAllStates(a: (List<S>) -> Boolean) {
    assert(a(_allStates))
  }
}
