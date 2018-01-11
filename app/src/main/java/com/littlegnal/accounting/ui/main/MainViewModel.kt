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

package com.littlegnal.accounting.ui.main

import android.support.annotation.VisibleForTesting
import com.littlegnal.accounting.base.eventbus.RxBus
import com.littlegnal.accounting.base.mvi.BaseViewModel
import com.littlegnal.accounting.base.mvi.MviAction
import com.littlegnal.accounting.base.mvi.MviIntent
import com.littlegnal.accounting.ui.addedit.AddOrEditEvent
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

/**
 * 主页[MainViewModel]
 */
class MainViewModel @Inject constructor(
    private val mainActionProcessorHolder: MainActionProcessorHolder,
    private val rxBus: RxBus
) : BaseViewModel<MainIntent, MainViewState>() {

  @VisibleForTesting
  val now: Calendar = Calendar.getInstance()

  override fun mergeExtraIntents(intents: Observable<MainIntent>): Observable<MainIntent> {
    return super.mergeExtraIntents(intents).mergeWith(extraIntents())
  }

  private fun extraIntents(): Observable<MainIntent>  =
      rxBus.asFlowable().toObservable()
          .filter { it is AddOrEditEvent }
          .map { it as AddOrEditEvent }
          .map {
            MainIntent.AddOrEditAccountingIntent(it.isAddedAccounting, it.accounting)
          }

  override fun compose(intentsSubject: PublishSubject<MainIntent>): Observable<MainViewState> =
      intentsSubject
          .compose(intentFilter)
          .map(this::actionFromIntent)
          .compose(mainActionProcessorHolder.actionProcessorWithReducer)
          .replay(1)
          .autoConnect(0)

  /**
   * 只取一次初始化[MviIntent]和其他[MviIntent]，过滤掉配置改变（如屏幕旋转）后重新传递过来的初始化
   * [MviIntent]，导致重新加载数据
   */
  private val intentFilter: ObservableTransformer<MainIntent, MainIntent> =
      ObservableTransformer { intents -> intents.publish { shared ->
          Observable.merge(
              shared.ofType(MainIntent.InitialIntent::class.java).take(1),
              shared.filter { it !is MainIntent.InitialIntent }
          )
        }
      }

  /**
   * 把[MviIntent]转换为[MviAction]
   */
  private fun actionFromIntent(mainIntent: MainIntent): MainAction {
    return when(mainIntent) {
      is MainIntent.InitialIntent -> { MainAction.LoadAccountingsAction(now.time) }
      is MainIntent.LoadNextPageIntent -> {
        MainAction.LoadAccountingsAction(mainIntent.lastDate)
      }
      is MainIntent.DeleteAccountingIntent -> {
        MainAction.DeleteAccountingAction(mainIntent.deletedId)
      }
      is MainIntent.AddOrEditAccountingIntent -> {
        if (mainIntent.isAddedAccounting) {
          MainAction.AddAccountingAction(mainIntent.accounting)
        } else {
          MainAction.UpdateAccountingAction(mainIntent.accounting)
        }
      }
    }
  }

}