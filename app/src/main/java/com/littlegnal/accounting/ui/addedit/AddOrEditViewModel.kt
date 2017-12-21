package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.eventbus.RxBus
import com.littlegnal.accounting.base.mvibase.BaseViewModel
import com.littlegnal.accounting.base.mvibase.LceStatus
import com.littlegnal.accounting.db.Accounting
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/12/16
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

  private val intentFilter: ObservableTransformer<AddOrEditIntent, AddOrEditIntent> =
      ObservableTransformer { intents -> intents.publish { shared ->
        Observable.merge(
            shared.ofType(AddOrEditIntent.InitialIntent::class.java).take(1),
            shared.filter { it !is AddOrEditIntent.InitialIntent } )
        }
      }

  private fun actionFromIntent(intent: AddOrEditIntent): AddOrEditAction =
      when(intent) {
        is AddOrEditIntent.InitialIntent ->
          if (intent.accountingId == null)
            AddOrEditAction.SkipAction()
          else
            AddOrEditAction.LoadAccountingAction(intent.accountingId)
        is AddOrEditIntent.CreateOrUpdateIntent ->
          if (intent.accountingId == null)
            AddOrEditAction.CreateAccountingAction(
                intent.amount,
                intent.tagName,
                intent.showDate,
                intent.remarks)
          else
            AddOrEditAction.UpdateAccountingAction(
                intent.accountingId,
                intent.amount,
                intent.tagName,
                intent.showDate,
                intent.remarks)
      }

  private val reducer: BiFunction<AddOrEditViewState, AddOrEditResult, AddOrEditViewState> =
      BiFunction { previousState, result ->
        Timber.e("AddOrEdit reducer thread name: ${Thread.currentThread().name}")
        when(result) {
          is AddOrEditResult.LoadAccountingResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> {
                val accounting: Accounting? = result.accounting
                previousState.copy(
                    isLoading = false,
                    error = null,
                    amount = accounting?.amount.toString(),
                    tagName = accounting?.tagName,
                    dateTime = addOrEditActionProcessorHolder.formatDate(accounting?.createTime),
                    remarks = accounting?.remarks,
                    isNeedFinish = true)
              }
              LceStatus.FAILURE ->
                previousState.copy(isLoading = false, error = result.error, isNeedFinish = false)
              LceStatus.IN_FLIGHT ->
                previousState.copy(isLoading = true, error = null, isNeedFinish = false)
            }
          }
          is AddOrEditResult.UpdateAccountingResult,
          is AddOrEditResult.CreateAccountingResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> {
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