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

import com.littlegnal.accounting.base.mvi.LceStatus
import com.littlegnal.accounting.base.mvi.MviResult
import com.littlegnal.accounting.db.Accounting

sealed class AddOrEditResult : MviResult {

  lateinit var status: LceStatus

  var error: Throwable? = null

  var accounting: Accounting? = null

  class LoadAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
        LoadAccountingResult().apply {
          status = LceStatus.SUCCESS
          LoadAccountingResult@ this.accounting = accounting
        }

      fun failure(error: Throwable?) =
        LoadAccountingResult().apply {
          status = LceStatus.FAILURE
          LoadAccountingResult@ this.error = error
        }

      fun inFlight() = LoadAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }

  class CreateAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
        CreateAccountingResult().apply {
          status = LceStatus.SUCCESS
          CreateAccountingResult@ this.accounting = accounting
        }

      fun failure(error: Throwable?) =
        CreateAccountingResult().apply {
          status = LceStatus.FAILURE
          CreateAccountingResult@ this.error = error
        }

      fun inFlight() = CreateAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }

  class UpdateAccountingResult : AddOrEditResult() {
    companion object {
      fun success(accounting: Accounting) =
        UpdateAccountingResult().apply {
          status = LceStatus.SUCCESS
          UpdateAccountingResult@ this.accounting = accounting
        }

      fun failure(error: Throwable) =
        UpdateAccountingResult().apply {
          status = LceStatus.FAILURE
          UpdateAccountingResult@ this.error = error
        }

      fun inFlight() = UpdateAccountingResult().apply { status = LceStatus.IN_FLIGHT }
    }
  }
}