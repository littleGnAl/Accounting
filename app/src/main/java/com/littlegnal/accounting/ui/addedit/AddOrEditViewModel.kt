package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.base.MvRxViewModel
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.MainActivity
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class AddOrEditViewModel @AssistedInject constructor(
  @Assisted initialState: AddOrEditMvRxViewState,
  @Assisted stateStore: MvRxStateStore<AddOrEditMvRxViewState>,
  private val accountingDao: AccountingDao
) : MvRxViewModel<AddOrEditMvRxViewState>(initialState, stateStore) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
      initialState: AddOrEditMvRxViewState,
      stateStore: MvRxStateStore<AddOrEditMvRxViewState>
    ): AddOrEditViewModel
  }

  init {
    loadAccounting(initialState.id)
  }

  fun loadAccounting(id: Int = -1) {
    if (id == -1) return
    accountingDao.getAccountingById(id)
        .toObservable()
        .subscribeOn(Schedulers.io())
        .execute {
          when (it) {
            is Loading -> {
              copy(isLoading = true, error = null)
            }
            is Success -> {
              val accounting = it()!!
              copy(
                isLoading = false,
                error = null,
                amount = accounting.amount.toString(),
                tagName = accounting.tagName,
                dateTime = dateTimeFormat.format(accounting.createTime),
                remarks = accounting.remarks
              )
            }
            is Fail -> {
              copy(isLoading = false, error = it.error)
            }
            else -> { copy() }
          }
        }
  }

  companion object : MvRxViewModelFactory<AddOrEditMvRxViewState> {

    @VisibleForTesting
    @SuppressLint("SimpleDateFormat")
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

    @JvmStatic override fun create(
      activity: FragmentActivity,
      state: AddOrEditMvRxViewState
    ): BaseMvRxViewModel<AddOrEditMvRxViewState> {
      return (activity as MainActivity).addOrEditViewModelFactory
          .create(state, RealMvRxStateStore(state))
    }
  }
}
