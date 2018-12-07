package com.littlegnal.accounting.ui.main

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.littlegnal.accounting.R
import com.littlegnal.accounting.R.string
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
import com.littlegnal.accounting.base.TestMvRxStateStore
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.MainViewModel.Companion.ONE_PAGE_SIZE
import com.littlegnal.accounting.ui.main.MainViewModel.Companion.dateTimeFormat
import com.littlegnal.accounting.ui.main.MainViewModel.Companion.oneDayFormat
import com.littlegnal.accounting.ui.main.MainViewModel.Companion.timeFormat
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyLong
import org.mockito.MockitoAnnotations
import java.util.Calendar

class MainViewModelTest {

  @Rule
  @JvmField
  val scheduler = RxImmediateSchedulerRule()

  @Mock
  private lateinit var applicationContext: Context

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var mainViewModel: MainViewModel

  private lateinit var accountings: List<Accounting>

  private lateinit var adapterList: List<MainAccountingDetail>

  private lateinit var owner: TestLifecycleOwner

  private lateinit var testStateStore: TestMvRxStateStore<MainState>

  private val calendar: Calendar = Calendar.getInstance()
      .apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  private fun initViewModel(initialState: MainState) {
    testStateStore = TestMvRxStateStore.create(initialState)
    mainViewModel = MainViewModel(
        initialState,
        testStateStore,
        accountingDao,
        applicationContext)
  }

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    owner = TestLifecycleOwner()
    owner.lifecycle.markState(Lifecycle.State.STARTED)

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
              oneDayFormat.format(accountingCalendar.time),
              "共(¥100.00)"
          )
      )
      tempAdapterList.add(
          MainAccountingDetailContent(
              accounting.id,
              "¥100.00",
              accounting.tagName,
              accounting.remarks,
              timeFormat.format(accounting.createTime),
              accounting.createTime
          )
      )

      tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
    }
    accountings = tempAccountings
    adapterList = tempAdapterList
  }

  @Test
  fun loadList_firstPage_success() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)

    val loadingState = MainState(isLoading = true, lastDate = calendar.time)
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList
    )

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong()))
        .thenReturn(100.0f)
    `when`(applicationContext.getString(string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    testStateStore.testAllStates { stateList ->
      stateList.size == 3 && stateList[1] == loadingState && stateList[2] == firstPageState
    }
  }

  @Test
  fun loadList_firstPage_whenNoDatas() {
    val loadingState = MainState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoMoreData = false,
        isNoData = false,
        lastDate = calendar.time
    )
    val firstPageState = loadingState.copy(isLoading = false, isNoMoreData = true, isNoData = true)

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(listOf()))

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    testStateStore.testAllStates { stateList ->
      stateList.size == 3 && stateList[1] == loadingState && stateList[2] == firstPageState
    }
  }

  @Test
  fun loadList_firstPage_whenNoMoreData() {
    val firstPage = accountings.take(ONE_PAGE_SIZE - 1)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE - 2)

    val loadingState = MainState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false,
        lastDate = calendar.time
    )
    val firstPageState = loadingState.copy(
        isLoading = false,
        accountingDetailList = firstPageAdapterList,
        isNoMoreData = true
    )

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f))
        .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    testStateStore.testAllStates { stateList ->
      stateList.size == 3 && stateList[1] == loadingState && stateList[2] == firstPageState
    }
  }

  @Test
  fun addOrEditAccounting_addAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val insertAccounting = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "gg"
    )
    val addedAccounting = insertAccounting.copy().apply { id = 3 * ONE_PAGE_SIZE + 1 }
    val addedContent = MainAccountingDetailContent(
        addedAccounting.id,
        "¥100.00",
        addedAccounting.tagName,
        addedAccounting.remarks,
        timeFormat.format(addedAccounting.createTime),
        addedAccounting.createTime
    )

    val loadingState = MainState(
        isLoading = true,
        error = null,
        accountingDetailList = listOf(),
        isNoData = false,
        isNoMoreData = false,
        lastDate = calendar.time
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

    `when`(accountingDao.insertAccounting(insertAccounting)).thenReturn(addedAccounting.id.toLong())
    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(R.string.main_accounting_detail_header_sum, 100.0f))
    .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    mainViewModel.addOrEditAccounting(
        accountingDetailList = firstPageAdapterList,
        accountingId = -1,
        amount = addedAccounting.amount,
        tagName = addedAccounting.tagName,
        showDate = dateTimeFormat.format(addedAccounting.createTime),
        remarks = addedAccounting.remarks)

    testStateStore.testAllStates { stateList ->
      stateList.size == 5 && stateList[4] == addedState
    }
  }

  @Test
  fun addOrEditAccounting_addAccounting_whenNoDatas() {
    val insertAccounting = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "gg"
    )
    val addedAccounting = insertAccounting.copy().apply { id = 3 * ONE_PAGE_SIZE + 1 }
    val addedHeader = MainAccountingDetailHeader(
        oneDayFormat.format(addedAccounting.createTime),
        "共(¥100.00)"
    )
    val addedContent = MainAccountingDetailContent(
        addedAccounting.id,
        "¥100.00",
        addedAccounting.tagName,
        addedAccounting.remarks,
        timeFormat.format(addedAccounting.createTime),
        addedAccounting.createTime
    )

    val addedState = MainState(
        isLoading = false,
        error = null,
        accountingDetailList = listOf(addedHeader, addedContent),
        isNoMoreData = true,
        isNoData = false,
        lastDate = calendar.time
    )

    `when`(accountingDao.insertAccounting(insertAccounting)).thenReturn(addedAccounting.id.toLong())
    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(listOf()))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
    .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    mainViewModel.addOrEditAccounting(
        accountingDetailList = emptyList(),
        accountingId = -1,
        amount = addedAccounting.amount,
        tagName = addedAccounting.tagName,
        showDate = dateTimeFormat.format(addedAccounting.createTime),
        remarks = addedAccounting.remarks)

    testStateStore.testAllStates { stateList ->
      stateList.size == 5 && stateList[4] == addedState
    }
  }

  @Test
  fun addOrEditAccounting_updateAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val updatedAccounting = firstPage[0].apply { this.tagName = "123" }
    val updatedAdapterItem = MainAccountingDetailContent(
        updatedAccounting.id,
        "¥100.00",
        updatedAccounting.tagName,
        updatedAccounting.remarks,
        timeFormat.format(updatedAccounting.createTime),
        updatedAccounting.createTime
    )

    val updatedState = MainState(
        isLoading = false,
        error = null,
        accountingDetailList = firstPageAdapterList.let {
          it.toMutableList()
              .apply { set(1, updatedAdapterItem) }
        },
        isNoData = false,
        isNoMoreData = false,
        lastDate = calendar.time
    )

    `when`(accountingDao.insertAccounting(updatedAccounting))
        .thenReturn(updatedAccounting.id.toLong())
    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
    .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    mainViewModel.addOrEditAccounting(
        accountingDetailList = firstPageAdapterList,
        accountingId = updatedAccounting.id,
        amount = updatedAccounting.amount,
        tagName = updatedAccounting.tagName,
        showDate = dateTimeFormat.format(updatedAccounting.createTime),
        remarks = updatedAccounting.remarks)

    testStateStore.testAllStates { stateList ->
      stateList.size == 5 && stateList[4] == updatedState
    }
  }

  @Test
  fun deleteAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val afterDeletedAdapterList = firstPageAdapterList
        .subList(2, firstPageAdapterList.size)

    val deletedState = MainState(
        isLoading = false,
        error = null,
        accountingDetailList = afterDeletedAdapterList,
        isNoData = false,
        isNoMoreData = false,
        lastDate = calendar.time
    )

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(
        applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f
        )
    )
    .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    initViewModel(initialState)

    mainViewModel.deleteAccounting(firstPageAdapterList, 1)

    Mockito.verify(accountingDao, Mockito.times(1)).deleteAccountingById(1)

    testStateStore.testAllStates { stateList ->
      stateList.size == 5 && stateList[4] == deletedState
    }
  }
}
