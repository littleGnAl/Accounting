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

package com.littlegnal.accounting.ui.summary

import com.littlegnal.accounting.base.mvi.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * 汇总页面[MviViewModel]
 */
class SummaryViewModel @Inject constructor(
    private val summaryActionProcessorHolder: SummaryActionProcessorHolder
) : BaseViewModel<SummaryIntent, SummaryViewState>() {

  override fun compose(intentsSubject: PublishSubject<SummaryIntent>):
      Observable<SummaryViewState> =
      intentsSubject
          .compose(intentFilter)
          .map(this::actionFromIntent)
          .compose(summaryActionProcessorHolder.actionProcessor)
          .scan(SummaryViewState.idle(), reducer)
          .replay(1)
          .autoConnect(0)

  /**
   * 只取一次初始化[MviIntent]和其他[MviIntent]，过滤掉配置改变（如屏幕旋转）后重新传递过来的初始化
   * [MviIntent]，导致重新加载数据
   */
  private val intentFilter: ObservableTransformer<SummaryIntent, SummaryIntent> =
      ObservableTransformer { intents -> intents.publish { shared ->
          Observable.merge(
              shared.ofType(SummaryIntent.InitialIntent::class.java).take(1),
              shared.filter { it !is SummaryIntent.InitialIntent }
          )
        }
      }

  /**
   * 把[MviIntent]转换为[MviAction]
   */
  private fun actionFromIntent(summaryIntent: SummaryIntent): SummaryAction =
      when(summaryIntent) {
        is SummaryIntent.InitialIntent -> {
          SummaryAction.InitialAction()
        }
        is SummaryIntent.SwitchMonthIntent -> {
          SummaryAction.SwitchMonthAction(summaryIntent.date)
        }
      }

  private val reducer = BiFunction<SummaryViewState, SummaryResult, SummaryViewState> {
        previousState, result ->
          when(result) {
            is SummaryResult.InitialResult -> {
              when(result.status) {
                LceStatus.SUCCESS -> {
                  previousState.copy(
                      isLoading = false,
                      error = null,
                      points = result.points,
                      months = result.months,
                      values = result.values,
                      selectedIndex = result.selectedIndex,
                      summaryItemList = result.summaryItemList,
                      isSwitchMonth = false)
                }
                LceStatus.FAILURE -> {
                  previousState.copy(isLoading = false, error = result.error)
                }
                LceStatus.IN_FLIGHT -> {
                  previousState.copy(isLoading = true, error = null)
                }
              }
            }
            is SummaryResult.SwitchMonthResult -> {
              when(result.status) {
                LceStatus.SUCCESS -> {
                  previousState.copy(
                      isLoading = false,
                      error = null,
                      summaryItemList = result.summaryItemList,
                      isSwitchMonth = true)
                }
                LceStatus.FAILURE -> {
                  previousState.copy(
                      isLoading = false,
                      error = result.error,
                      isSwitchMonth = true)
                }
                LceStatus.IN_FLIGHT -> {
                  previousState.copy(
                      isLoading = true,
                      error = null,
                      isSwitchMonth = true)
                }
              }
            }
          }
      }

}