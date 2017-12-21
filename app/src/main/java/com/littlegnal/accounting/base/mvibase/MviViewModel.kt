package com.littlegnal.accounting.base.mvibase

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
