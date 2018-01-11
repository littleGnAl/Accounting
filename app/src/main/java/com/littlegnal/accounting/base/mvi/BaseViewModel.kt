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

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment
import com.littlegnal.accounting.base.eventbus.RxBus
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * 抽象[MviViewModel]，实现[MviIntent]与[MviViewState]的连接
 */
abstract class BaseViewModel<I : MviIntent, S : MviViewState> : ViewModel(), MviViewModel<I, S> {

  /**
   * 用于保持流存活的代理[PublishSubject]。
   *
   * 主要用于保持UI（[Activity], [Fragment]）在配置发生变化（屏幕旋转）的时候断开或者重新连接正在进行事件和
   * 上一次缓存状态存活
   */
  private val intentsSubject = PublishSubject.create<I>()
  private val statesObservable: Observable<S> by lazy { compose(intentsSubject) }

  override fun processIntents(intents: Observable<I>) {
    mergeExtraIntents(intents).subscribe(intentsSubject)
  }

  override fun states(): Observable<S> = statesObservable

  /**
   * 组合业务逻辑流
   */
  abstract fun compose(intentsSubject: PublishSubject<I>): Observable<S>

  /**
   * 该方法用于合并其他页面通知过来的[MviIntent], 如通过[RxBus]
   * 传递的[MviIntent]
   */
  open fun mergeExtraIntents(intents: Observable<I>) = intents
}