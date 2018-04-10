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
import com.littlegnal.accounting.base.schedulers.TestSchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.Calendar

class AddOrEditViewModelTest {

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var addOrEditViewModel: AddOrEditViewModel

  private lateinit var testObserver: TestObserver<AddOrEditViewState>

  @Mock
  private lateinit var rxBus: RxBus

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    addOrEditViewModel = AddOrEditViewModel(
        AddOrEditActionProcessorHolder(TestSchedulerProvider(), accountingDao), rxBus
    )
    testObserver = addOrEditViewModel.states()
        .test()
  }

  @Test
  fun test_initialIntent_edit() {
    val calendar = Calendar.getInstance()
        .apply {
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val account = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋"
    )
    account.id = 1

    `when`(accountingDao.getAccountingById(1)).thenReturn(Maybe.just(account))

    val loadingState = AddOrEditViewState(true, null)
    val updateState = loadingState.copy(
        isLoading = false,
        amount = account.amount.toString(),
        tagName = account.tagName,
        dateTime = DATE_TIME_FORMAT.format(account.createTime),
        remarks = account.remarks
    )

    addOrEditViewModel.processIntents(Observable.just(AddOrEditIntent.InitialIntent(1)))
    testObserver.assertValueAt(1, loadingState)
    testObserver.assertValueAt(2, updateState)
  }

  @Test
  fun test_CreateOrUpdateIntent_create() {
    val calendar = Calendar.getInstance()
        .apply {
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val account = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋"
    )

    val insertAccount = account.copy()
        .apply { id = 1 }

    `when`(accountingDao.insertAccounting(account)).thenReturn(1)

    val loadingState = AddOrEditViewState(isLoading = true, error = null, isNeedFinish = false)
    val createState = loadingState.copy(isLoading = false, isNeedFinish = true)

    addOrEditViewModel.processIntents(
        Observable.just(
            AddOrEditIntent.CreateOrUpdateIntent(
                null,
                account.amount,
                account.tagName,
                DATE_TIME_FORMAT.format(account.createTime),
                account.remarks
            )
        )
    )

    verify(accountingDao, times(1)).insertAccounting(account)
    verify(rxBus, times(1))
        .send(AddOrEditEvent(true, insertAccount))

    testObserver.assertValueAt(1, loadingState)
    testObserver.assertValueAt(2, createState)
  }

  @Test
  fun test_CreateOrUpdateIntent_update() {
    val calendar = Calendar.getInstance()
        .apply {
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val account = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋"
    )
    account.id = 1

    val updateAccounting = account.copy(amount = 200.0f)
        .apply { id = 2 }

    `when`(accountingDao.getAccountingById(1)).thenReturn(Maybe.just(account))
    `when`(accountingDao.insertAccounting(updateAccounting)).thenReturn(2)

    val loadingState = AddOrEditViewState(true, null)
    val initialState = loadingState.copy(
        isLoading = false,
        amount = account.amount.toString(),
        tagName = account.tagName,
        dateTime = DATE_TIME_FORMAT.format(account.createTime),
        remarks = account.remarks
    )

    val intents = Observable.merge(
        Observable.just(AddOrEditIntent.InitialIntent(1)),
        Observable.just(
            AddOrEditIntent.CreateOrUpdateIntent(
                null,
                updateAccounting.amount,
                updateAccounting.tagName,
                DATE_TIME_FORMAT.format(updateAccounting.createTime),
                updateAccounting.remarks
            )
        )
    )

    addOrEditViewModel.processIntents(intents)

    verify(accountingDao, times(1)).insertAccounting(updateAccounting)
    verify(rxBus, times(1))
        .send(AddOrEditEvent(true, updateAccounting))

    testObserver.assertValueAt(3, initialState.copy(isLoading = true))
    testObserver.assertValueAt(4, initialState.copy(isLoading = false, isNeedFinish = true))
  }

  companion object {
    private val DATE_TIME_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm")
  }
}