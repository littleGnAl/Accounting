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

import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.eventbus.RxBus
import com.littlegnal.accounting.base.schedulers.TestSchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.Calendar

class MainViewModelTest {

  @Mock
  private lateinit var applicationContext: Context

  @Mock
  private lateinit var accountingDao: AccountingDao

  private val rxBus = RxBus()

  private lateinit var mainViewModel: MainViewModel

  private lateinit var testObserver: TestObserver<MainViewState>

  private lateinit var accountings: List<Accounting>

  private lateinit var adapterList: List<MainAccountingDetail>

  private val calendar: Calendar = Calendar.getInstance()
      .apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    mainViewModel = MainViewModel(
        MainActionProcessorHolder(TestSchedulerProvider(), applicationContext, accountingDao),
        rxBus
    )
    testObserver = mainViewModel.states()
        .test()

    val tempAccountings = mutableListOf<Accounting>()
    val tempAdapterList = mutableListOf<MainAccountingDetail>()
    val tempCalendar = Calendar.getInstance()
        .apply { time = calendar.time }

    for (i in (0..(3 * ONE_PAGE_SIZE))) {
      val accountingCalendar = Calendar.getInstance()
          .apply { time = tempCalendar.time }
      val accounting = Accounting(
          100.0f,
          accountingCalendar.time,
          "早餐",
          "gg"
      ).apply { id = i + 1 }
      tempAccountings.add(accounting)

      tempAdapterList.add(
          MainAccountingDetailHeader(
              ONE_DAY_FORMAT.format(accountingCalendar.time),
              "共(¥100.00)"
          )
      )
      tempAdapterList.add(
          MainAccountingDetailContent(
              accounting.id,
              "¥100.00",
              accounting.tagName,
              accounting.remarks,
              TIME_FORMAT.format(accounting.createTime),
              accounting.createTime
          )
      )

      tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
    }
    accountings = tempAccountings
    adapterList = tempAdapterList
  }

  @Test
  fun test_initialIntent_firstPage() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)

    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    mainViewModel.processIntents(Observable.just(MainIntent.InitialIntent()))

    testObserver.assertValueAt(1, loadingState)
    testObserver.assertValueAt(2, firstPageState)
  }

  @Test
  fun test_initialIntent_firstPage_whenNoDatas() {
    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoMoreData = false,
        isNoData = false
    )
    val firstPageState = loadingState.copy(isLoading = false, isNoMoreData = true, isNoData = true)

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(listOf()))

    mainViewModel.processIntents(Observable.just(MainIntent.InitialIntent()))

    testObserver.assertValueAt(1, loadingState)
    testObserver.assertValueAt(2, firstPageState)
  }

  @Test
  fun test_initialIntent_firstPage_whenNoMoreData() {
    val firstPage = accountings.take(ONE_PAGE_SIZE - 1)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE - 2)

    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList,
        isNoMoreData = true
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    mainViewModel.processIntents(Observable.just(MainIntent.InitialIntent()))

    testObserver.assertValueAt(1, loadingState)
    testObserver.assertValueAt(2, firstPageState)
  }

  @Test
  fun test_loadNextPageIntent() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val secondPage = accountings.subList(ONE_PAGE_SIZE, 2 * ONE_PAGE_SIZE)
    val secondPageAdapterList = adapterList.subList(2 * ONE_PAGE_SIZE, 4 * ONE_PAGE_SIZE)

    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList
    )
    val secondPageLoadingState = firstPageState.copy(isLoading = true)
    val secondPageState = secondPageLoadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList.let {
          it.toMutableList()
              .apply { addAll(secondPageAdapterList) }
        })

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(
        accountingDao.queryPreviousAccounting(
            firstPage.last().createTime,
            ONE_PAGE_SIZE.toLong()
        )
    )
        .thenReturn(Maybe.just(secondPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(MainIntent.LoadNextPageIntent(firstPage.last().createTime))
    )

    mainViewModel.processIntents(intents)

    testObserver.assertValueAt(3, secondPageLoadingState)
    testObserver.assertValueAt(4, secondPageState)
  }

  @Test
  fun test_loadNextPageIntent_whenNoMoreData() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val secondPage = accountings.subList(ONE_PAGE_SIZE, 2 * ONE_PAGE_SIZE - 1)
    val secondPageAdapterList = adapterList.subList(2 * ONE_PAGE_SIZE, 4 * ONE_PAGE_SIZE - 2)

    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList
    )
    val secondPageLoadingState = firstPageState.copy(isLoading = true)
    val secondPageState = secondPageLoadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList.let {
          it.toMutableList()
              .apply { addAll(secondPageAdapterList) }
        },
        isNoMoreData = true
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(
        accountingDao.queryPreviousAccounting(
            firstPage.last().createTime,
            ONE_PAGE_SIZE.toLong()
        )
    )
        .thenReturn(Maybe.just(secondPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(MainIntent.LoadNextPageIntent(firstPage.last().createTime))
    )

    mainViewModel.processIntents(intents)

    testObserver.assertValueAt(3, secondPageLoadingState)
    testObserver.assertValueAt(4, secondPageState)
  }

  @Test
  fun test_addOrEditAccountingIntent_addAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val addedAccounting = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "gg"
    ).apply { id = 3 * ONE_PAGE_SIZE + 1 }
    val addedContent = MainAccountingDetailContent(
        addedAccounting.id,
        "¥100.00",
        addedAccounting.tagName,
        addedAccounting.remarks,
        TIME_FORMAT.format(addedAccounting.createTime),
        addedAccounting.createTime
    )

    val loadingState = MainViewState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList
    )
    val addedLoadingState = firstPageState.copy(isLoading = true)
    val addedState = addedLoadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList.let {
          it.toMutableList()
              .apply { add(1, addedContent) }
        })

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(
            MainIntent.AddOrEditAccountingIntent(
                true, addedAccounting
            )
        )
    )
    mainViewModel.processIntents(intents)

    testObserver.assertValueAt(3, addedState)
  }

  @Test
  fun test_addOrEditAccountingIntent_addAccounting_whenNoDatas() {
    val addedAccounting = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "gg"
    ).apply { id = 3 * ONE_PAGE_SIZE + 1 }
    val addedHeader = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(addedAccounting.createTime),
        "共(¥100.00)"
    )
    val addedContent = MainAccountingDetailContent(
        addedAccounting.id,
        "¥100.00",
        addedAccounting.tagName,
        addedAccounting.remarks,
        TIME_FORMAT.format(addedAccounting.createTime),
        addedAccounting.createTime
    )

    val addedState = MainViewState(
        isLoading = false,
        error = null,
        accountingDetailList = listOf(addedHeader, addedContent),
        isNoMoreData = true,
        isNoData = false
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(listOf()))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(
            MainIntent.AddOrEditAccountingIntent(
                true, addedAccounting
            )
        )
    )
    mainViewModel.processIntents(intents)

    testObserver.assertValueAt(3, addedState)
  }

  @Test
  fun test_addOrEditAccountingIntent_updateAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val updatedAccounting = firstPage[0].apply { this.tagName = "123" }
    val updatedAdapterItem = MainAccountingDetailContent(
        updatedAccounting.id,
        "¥100.00",
        updatedAccounting.tagName,
        updatedAccounting.remarks,
        TIME_FORMAT.format(updatedAccounting.createTime),
        updatedAccounting.createTime
    )

    val updatedState = MainViewState(
        isLoading = false,
        error = null,
        accountingDetailList = firstPageAdapterList.let {
          it.toMutableList()
              .apply { set(1, updatedAdapterItem) }
        },
        isNoData = false,
        isNoMoreData = false
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(
            MainIntent.AddOrEditAccountingIntent(
                false, updatedAccounting
            )
        )
    )

    mainViewModel.processIntents(intents)

    testObserver.assertValueAt(3, updatedState)
  }

  @Test
  fun test_deleteAccountingIntent() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val afterDeletedAdapterList = firstPageAdapterList
        .subList(2, firstPageAdapterList.size)

    val deletedState = MainViewState(
        isLoading = false,
        error = null,
        accountingDetailList = afterDeletedAdapterList,
        isNoData = false,
        isNoMoreData = false
    )

    `when`(accountingDao.queryPreviousAccounting(mainViewModel.now.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyString())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
        .thenReturn("共(¥100.00)")

    val intents = Observable.merge(
        Observable.just(MainIntent.InitialIntent()),
        Observable.just(MainIntent.DeleteAccountingIntent(1))
    )
    mainViewModel.processIntents(intents)

    verify(accountingDao, times(1)).deleteAccountingById(1)

    testObserver.assertValueAt(4, deletedState)
  }

  companion object {
    private val ONE_DAY_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    private val TIME_FORMAT: SimpleDateFormat = SimpleDateFormat("HH:mm")
  }
}