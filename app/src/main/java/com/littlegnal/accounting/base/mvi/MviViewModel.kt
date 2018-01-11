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

import io.reactivex.Observable

/**
 * 订阅[MviView]的意图[MviIntent]，处理业务逻辑后将[MviViewState]作为结果返回给[MviView]，来渲染界面
 *
 * @param <I> 发出的意图[MviIntent]
 * @param <S> 订阅状态[MviViewState]，用于渲染UI.
 * */
interface MviViewModel<I : MviIntent, S : MviViewState> {
  fun processIntents(intents: Observable<I>)

  fun states(): Observable<S>
}
