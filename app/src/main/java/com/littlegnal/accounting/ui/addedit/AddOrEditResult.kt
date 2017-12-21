package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvibase.LceStatus
import com.littlegnal.accounting.db.Accounting

/**
 * @author littlegnal
 * @date 2017/12/16
 */
sealed class AddOrEditResult {

  lateinit var status: LceStatus

  var error: Throwable? = null

  var accounting: Accounting? = null

  class LoadAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
          LoadAccountingResult().apply {
            status = LceStatus.SUCCESS
            LoadAccountingResult@this.accounting = accounting
          }

      fun failure(error: Throwable?) =
          LoadAccountingResult().apply {
            status = LceStatus.FAILURE
            LoadAccountingResult@this.error = error
          }

      fun inFlight() = LoadAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }

  class CreateAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
          CreateAccountingResult().apply {
            status = LceStatus.SUCCESS
            CreateAccountingResult@this.accounting = accounting
          }

      fun failure(error: Throwable?) =
          CreateAccountingResult().apply {
            status = LceStatus.FAILURE
            CreateAccountingResult@this.error = error
          }

      fun inFlight() = CreateAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }

  class UpdateAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
          UpdateAccountingResult().apply {
            status = LceStatus.SUCCESS
            UpdateAccountingResult@this.accounting = accounting
          }

      fun failure(error: Throwable) =
          UpdateAccountingResult().apply {
            status = LceStatus.FAILURE
            UpdateAccountingResult@this.error = error
          }

      fun inFlight() = UpdateAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }
}