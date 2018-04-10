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

package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.eventbus.RxBus
import com.littlegnal.accounting.base.mvi.BaseViewModel
import com.littlegnal.accounting.base.mvi.LceStatus
import com.littlegnal.accounting.base.mvi.MviAction
import com.littlegnal.accounting.base.mvi.MviIntent
import com.littlegnal.accounting.base.mvi.MviResult
import com.littlegnal.accounting.base.mvi.MviView
import com.littlegnal.accounting.base.mvi.MviViewModel
import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.db.Accounting
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * 增加或修改页面[MviViewModel]
 */
class AddOrEditViewModel @Inject constructor(
  private val addOrEditActionProcessorHolder: AddOrEditActionProcessorHolder,
  private val rxBus: RxBus
) : BaseViewModel<AddOrEditIntent, AddOrEditViewState>() {

  override fun compose(intentsSubject: PublishSubject<AddOrEditIntent>):
      Observable<AddOrEditViewState> {
    return intentsSubject
        .compose(intentFilter)
        .map(this::actionFromIntent)
        .filter { it !is AddOrEditAction.SkipAction }
        .compose(addOrEditActionProcessorHolder.actionProcessor)
        .scan(AddOrEditViewState.idle(), reducer)
        .replay(1)
        .autoConnect(0)
  }

  /**
   * 只取一次初始化[MviIntent]和其他[MviIntent]，过滤掉配置改变（如屏幕旋转）后重新传递过来的初始化
   * [MviIntent]，导致重新加载数据
   */
  private val intentFilter: ObservableTransformer<AddOrEditIntent, AddOrEditIntent> =
    ObservableTransformer { intents ->
      intents.publish { shared ->
        Observable.merge(
            shared.ofType(AddOrEditIntent.InitialIntent::class.java).take(1),
            shared.filter { it !is AddOrEditIntent.InitialIntent })
      }
    }

  /**
   * 把[MviIntent]转换为[MviAction]
   */
  private fun actionFromIntent(intent: AddOrEditIntent): AddOrEditAction =
    when (intent) {
      is AddOrEditIntent.InitialIntent -> {
        if (intent.accountingId == null) {
          AddOrEditAction.SkipAction()
        } else {
          AddOrEditAction.LoadAccountingAction(intent.accountingId)
        }
      }

      is AddOrEditIntent.CreateOrUpdateIntent -> {
        if (intent.accountingId == null) {
          AddOrEditAction.CreateAccountingAction(
              intent.amount,
              intent.tagName,
              intent.showDate,
              intent.remarks
          )
        } else {
          AddOrEditAction.UpdateAccountingAction(
              intent.accountingId,
              intent.amount,
              intent.tagName,
              intent.showDate,
              intent.remarks
          )
        }
      }
    }

  /**
   * 使用最后一次缓存的[MviViewState]和最新的[MviResult]来创建新的[MviViewState]，通过[MviView.render]方法
   * 把新的[MviViewState]渲染到界面
   */
  private val reducer: BiFunction<AddOrEditViewState, AddOrEditResult, AddOrEditViewState> =
    BiFunction { previousState, result ->
      when (result) {
        is AddOrEditResult.LoadAccountingResult -> {
          when (result.status) {
            LceStatus.SUCCESS -> {
              val accounting: Accounting? = result.accounting
              previousState.copy(
                  isLoading = false,
                  error = null,
                  amount = accounting?.amount.toString(),
                  tagName = accounting?.tagName,
                  dateTime = addOrEditActionProcessorHolder.formatDate(accounting?.createTime),
                  remarks = accounting?.remarks,
                  isNeedFinish = false
              )
            }
            LceStatus.FAILURE ->
              previousState.copy(isLoading = false, error = result.error, isNeedFinish = false)
            LceStatus.IN_FLIGHT ->
              previousState.copy(isLoading = true, error = null, isNeedFinish = false)
          }
        }
        is AddOrEditResult.UpdateAccountingResult,
        is AddOrEditResult.CreateAccountingResult -> {
          when (result.status) {
            LceStatus.SUCCESS -> {
              result.accounting?.also {
                rxBus.send(AddOrEditEvent(result is AddOrEditResult.CreateAccountingResult, it))
              }
              previousState.copy(isLoading = false, error = null, isNeedFinish = true)
            }
            LceStatus.FAILURE ->
              previousState.copy(isLoading = false, error = result.error, isNeedFinish = false)
            LceStatus.IN_FLIGHT ->
              previousState.copy(isLoading = true, error = null, isNeedFinish = false)
          }
        }
      }
    }
}