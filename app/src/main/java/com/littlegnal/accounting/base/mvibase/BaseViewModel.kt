package com.littlegnal.accounting.base.mvibase

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * 抽象[MviViewModel]，实现[MviIntent]与[MviViewState]的连接
 * @author littlegnal
 * @date 2017/12/11
 */
abstract class BaseViewModel<I : MviIntent, S : MviViewState> : ViewModel(), MviViewModel<I, S> {

  private val intentsSubject = PublishSubject.create<I>()
  private val statesObservable: Observable<S> by lazy { compose(intentsSubject) }

  override fun processIntents(intents: Observable<I>) {
    intents.subscribe(intentsSubject)
  }

  override fun states(): Observable<S> = statesObservable

  abstract fun compose(intentsSubject: PublishSubject<I>): Observable<S>
}