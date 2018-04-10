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

import com.littlegnal.accounting.base.mvi.LceStatus
import com.littlegnal.accounting.base.mvi.MviResult
import com.littlegnal.accounting.db.Accounting
import java.util.Date

sealed class MainResult : MviResult {

  data class LoadAccountingsResult(
    val status: LceStatus,
    val error: Throwable?,
    val lastDate: Date,
    val accountingList: List<Accounting>
  ) : MainResult() {
    companion object {
      fun success(
        lastDate: Date,
        accountingList: List<Accounting>
      ) =
        LoadAccountingsResult(
            LceStatus.SUCCESS,
            null,
            lastDate,
            accountingList
        )

      fun failure(error: Throwable?) =
        LoadAccountingsResult(
            LceStatus.FAILURE,
            error,
            Date(),
            listOf()
        )

      fun inFlight() =
        LoadAccountingsResult(
            LceStatus.IN_FLIGHT,
            null,
            Date(),
            listOf()
        )
    }
  }

  data class DeleteAccountingResult(
    val status: LceStatus,
    val error: Throwable?,
    val deletedId: Int
  ) : MainResult() {
    companion object {

      fun success(deletedId: Int) =
        DeleteAccountingResult(LceStatus.SUCCESS, null, deletedId)

      fun failure(error: Throwable?) =
        DeleteAccountingResult(LceStatus.FAILURE, error, -1)

      fun inFlight() =
        DeleteAccountingResult(LceStatus.IN_FLIGHT, null, -1)
    }
  }

  data class AddAccountingResult(
    val status: LceStatus,
    val error: Throwable?,
    val accounting: Accounting
  ) : MainResult() {
    companion object {
      fun success(accounting: Accounting) =
        AddAccountingResult(LceStatus.SUCCESS, null, accounting)
    }
  }

  data class UpdateAccountingResult(
    val status: LceStatus,
    val error: Throwable?,
    val accounting: Accounting
  ) : MainResult() {
    companion object {
      fun success(accounting: Accounting) =
        UpdateAccountingResult(LceStatus.SUCCESS, null, accounting)
    }
  }
}