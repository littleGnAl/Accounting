package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author littlegnal
 * @date 2017/12/16
 */
class AddOrEditActionProcessorHolder(
    private val schedulerProvider: BaseSchedulerProvider,
    private val accountingDao: AccountingDao
) {

  @SuppressLint("SimpleDateFormat")
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

  private val loadDataProcessor =
      ObservableTransformer<AddOrEditAction.LoadAccountingAction,
          AddOrEditResult.LoadAccountingResult> { actions -> actions.flatMap {
        action -> accountingDao.getAccountingById(action.accountingId)
            .toObservable()
            .map { AddOrEditResult.LoadAccountingResult.success(it) }
            .onErrorReturn { AddOrEditResult.LoadAccountingResult.failure(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith { AddOrEditResult.LoadAccountingResult.inFlight() }
        }
      }

  private val createAccountingProcessor =
      ObservableTransformer<AddOrEditAction.CreateAccountingAction,
           AddOrEditResult.CreateAccountingResult> {
        actions -> actions.flatMap {
          action -> Observable.fromCallable {
              Timber.e("action - " + action)
              val accounting = Accounting(
                  action.amount,
                  dateTimeFormat.parse(action.showDate),
                  action.tagName,
                  action.remarks)
              val insertedId = accountingDao.insertAccounting(accounting)
              accounting.id = insertedId.toInt()
              Timber.e("accounting - " + accounting)
              accounting
            }
            .map { AddOrEditResult.CreateAccountingResult.success(it) }
            .onErrorReturn { AddOrEditResult.CreateAccountingResult.failure(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith { AddOrEditResult.CreateAccountingResult.inFlight() }
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
          .onErrorReturn { AddOrEditResult.UpdateAccountingResult.failure(it) }
          .subscribeOn(schedulerProvider.io())
          .observeOn(schedulerProvider.ui())
          .startWith { AddOrEditResult.UpdateAccountingResult.inFlight() }
        }
      }

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

  fun formatDate(date: Date?): String = dateTimeFormat.format(date)
}