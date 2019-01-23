package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.littlegnal.accounting.base.MvRxViewModel
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.MainActivity
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class AddOrEditViewModel @AssistedInject constructor(
  @Assisted initialState: AddOrEditMvRxViewState,
  private val accountingDao: AccountingDao
) : MvRxViewModel<AddOrEditMvRxViewState>(initialState) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
      initialState: AddOrEditMvRxViewState
    ): AddOrEditViewModel
  }

  fun loadAccounting(id: Int = -1) {
    if (id == -1) return
    withState { state ->
      if (state.accounting !is Uninitialized) return@withState

      accountingDao.getAccountingById(id)
          .toObservable()
          .subscribeOn(Schedulers.io())
          .execute {
            when (it) {
              is Loading -> {
                copy(accounting = it)
              }
              is Success -> {
                val accounting = it()!!
                copy(
                    accounting = it,
                    amount = accounting.amount.toString(),
                    tagName = accounting.tagName,
                    dateTime = dateTimeFormat.format(accounting.createTime),
                    remarks = accounting.remarks
                )
              }
              is Fail -> {
                copy(accounting = it)
              }
              else -> { copy() }
            }
          }
    }
  }

  companion object : MvRxViewModelFactory<AddOrEditViewModel, AddOrEditMvRxViewState> {

    @VisibleForTesting
    @SuppressLint("SimpleDateFormat")
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

    override fun create(
      viewModelContext: ViewModelContext,
      state: AddOrEditMvRxViewState
    ): AddOrEditViewModel? {
      return (viewModelContext.activity as MainActivity).addOrEditViewModelFactory.create(state)
    }
  }
}
