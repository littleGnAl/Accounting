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

package com.littlegnal.accounting.base.mvi

import androidx.lifecycle.ViewModel
import com.littlegnal.accounting.base.eventbus.RxBus
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Abstract implementation of [MviViewModel], connecting [MviIntent] and [MviViewState]
 */
abstract class BaseViewModel<I : MviIntent, S : MviViewState> : ViewModel(), MviViewModel<I, S> {

  /**
   * Proxy subject used to keep the stream alive even after the UI gets recycled.
   * This is basically used to keep ongoing events and the last cached State alive
   * while the UI disconnects and reconnects on config changes.
   */
  private val intentsSubject = PublishSubject.create<I>()
  private val statesObservable: Observable<S> by lazy { compose(intentsSubject) }

  override fun processIntents(intents: Observable<I>) {
    mergeExtraIntents(intents).subscribe(intentsSubject)
  }

  override fun states(): Observable<S> = statesObservable

  /**
   * Compose all components to create the stream logic
   */
  abstract fun compose(intentsSubject: PublishSubject<I>): Observable<S>

  /**
   * This method is used to merge [MviIntent] notified by other pages, such as [MviIntent] passed
   * by [RxBus]
   */
  open fun mergeExtraIntents(intents: Observable<I>) = intents
}
