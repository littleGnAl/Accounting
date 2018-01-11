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

import android.annotation.SuppressLint
import com.littlegnal.accounting.base.mvi.MviAction
import com.littlegnal.accounting.base.mvi.MviResult
import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用于处理所有[MviAction]的业务逻辑，并把[MviAction]转换成[MviResult]
 */
class AddOrEditActionProcessorHolder(
    private val schedulerProvider: BaseSchedulerProvider,
    private val accountingDao: AccountingDao) {

  @SuppressLint("SimpleDateFormat")
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

  private val loadDataProcessor =
      ObservableTransformer<AddOrEditAction.LoadAccountingAction,
          AddOrEditResult.LoadAccountingResult> { actions -> actions.flatMap {
        action -> accountingDao.getAccountingById(action.accountingId)
            .toObservable()
            .map { AddOrEditResult.LoadAccountingResult.success(it) }
            .onErrorReturn {
                Timber.e(it)
                AddOrEditResult.LoadAccountingResult.failure(it)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(AddOrEditResult.LoadAccountingResult.inFlight())
        }
      }

  private val createAccountingProcessor =
      ObservableTransformer<AddOrEditAction.CreateAccountingAction,
           AddOrEditResult.CreateAccountingResult> {
        actions -> actions.flatMap {
          action -> Observable.fromCallable {
              val accounting = Accounting(
                  action.amount,
                  dateTimeFormat.parse(action.showDate),
                  action.tagName,
                  action.remarks)
              val insertedId = accountingDao.insertAccounting(accounting)
              accounting.id = insertedId.toInt()
              accounting
            }
            .map { AddOrEditResult.CreateAccountingResult.success(it) }
            .onErrorReturn {
                Timber.e(it)
                AddOrEditResult.CreateAccountingResult.failure(it)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(AddOrEditResult.CreateAccountingResult.inFlight())
        }
      }

  private val updateAccountingProcessor =
      ObservableTransformer<AddOrEditAction.UpdateAccountingAction,
          AddOrEditResult.UpdateAccountingResult> { actions -> actions.flatMap {
        action -> Observable.fromCallable {
            val accounting = Accounting(
                action.amount,
                dateTimeFormat.parse(action.showDate),
                action.tagName,
                action.remarks).apply { id = action.accountingId }
            accountingDao.insertAccounting(accounting)
            accounting
          }
          .map { AddOrEditResult.UpdateAccountingResult.success(it) }
          .onErrorReturn {
              Timber.e(it)
              AddOrEditResult.UpdateAccountingResult.failure(it)
          }
          .subscribeOn(schedulerProvider.io())
          .observeOn(schedulerProvider.ui())
          .startWith(AddOrEditResult.UpdateAccountingResult.inFlight())
        }
      }

  /**
   * 拆分[Observable<MviAction>]并且为不同的[MviAction]提供相应的processor，processor用于处理业务逻辑，
   * 同时把[MviAction]转换为[MviResult]，最终通过[Observable.merge]合并回一个流
   *
   * 为了防止遗漏[MviAction]未处理，在流的最后合并一个错误检测，方便维护
   */
  val actionProcessor: ObservableTransformer<AddOrEditAction, AddOrEditResult> =
      ObservableTransformer { actions -> actions.publish {
        shared -> Observable.merge(
          shared.ofType(AddOrEditAction.LoadAccountingAction::class.java)
              .compose(loadDataProcessor),
          shared.ofType(AddOrEditAction.CreateAccountingAction::class.java)
              .compose<AddOrEditResult>(createAccountingProcessor),
          shared.ofType(AddOrEditAction.UpdateAccountingAction::class.java)
              .compose(updateAccountingProcessor))
          .mergeWith(shared.filter {
                it !is AddOrEditAction.LoadAccountingAction &&
                    it !is AddOrEditAction.CreateAccountingAction &&
                    it !is AddOrEditAction.UpdateAccountingAction
              }
              .flatMap {
                Observable.error<AddOrEditResult>(
                    IllegalArgumentException("Unknown Action type: $it"))
              })

        }
      }

  /**
   * 以*yyyy/MM/dd HH:mm*的格式输出日期
   */
  fun formatDate(date: Date?): String = dateTimeFormat.format(date)
}