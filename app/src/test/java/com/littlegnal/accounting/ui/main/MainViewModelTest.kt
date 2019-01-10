package com.littlegnal.accounting.ui.main

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.R
import com.littlegnal.accounting.R.string
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
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
import org.mockito.MockitoAnnotations
import java.util.Calendar
import kotlin.reflect.KProperty1

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

  private val calendar: Calendar = Calendar.getInstance()
      .apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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
          amount = 100.0f,
          createTime = accountingCalendar.time,
          tagName = "早餐",
          remarks = "gg"
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

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong()))
        .thenReturn(100.0f)
    `when`(applicationContext.getString(string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    val data = mutableListOf<Async<List<MainAccountingDetail>>>()
    mainViewModel.selectSubscribe(owner, MainState::accountingDetailList) {
      data.add(it)
    }
    mainViewModel.loadFirstPage()

    assert(data.size == 3 &&
        data[1] is Loading &&
        data[2] is Success &&
        data[2]() == firstPageAdapterList)

    withState(mainViewModel) {
      assert(!it.isNoMoreData)
      assert(!it.isNoData)
    }
  }

  @Test
  fun loadList_firstPage_whenNoDatas() {
    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(listOf()))

    val initialState = MainState(lastDate = calendar.time)
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    val data = mutableListOf<Async<List<MainAccountingDetail>>>()
    mainViewModel.selectSubscribe(owner, MainState::accountingDetailList) {
      data.add(it)
    }

    mainViewModel.loadFirstPage()

    assert(data.size == 3 &&
        data[1] is Loading &&
        data[2] is Success &&
        data[2]() == listOf<MainAccountingDetail>())

    withState(mainViewModel) {
      assert(it.isNoMoreData)
      assert(it.isNoData)
    }
  }

  @Test
  fun loadList_firstPage_whenNoMoreData() {
    val firstPage = accountings.take(ONE_PAGE_SIZE - 1)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE - 2)

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
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    val data = mutableListOf<Async<List<MainAccountingDetail>>>()
    mainViewModel.selectSubscribe(owner, MainState::accountingDetailList) {
      data.add(it)
    }

    mainViewModel.loadFirstPage()
    assert(data.size == 3 &&
        data[1] is Loading &&
        data[2] is Success &&
        data[2]() == firstPageAdapterList)

    withState(mainViewModel) {
      assert(it.isNoMoreData)
      assert(!it.isNoData)
    }
  }

  @Test
  fun loadNextPage_success() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val secondPage = accountings.subList(ONE_PAGE_SIZE, 2 * ONE_PAGE_SIZE)
    val secondPageAdapterList = adapterList.subList(2 * ONE_PAGE_SIZE, 4 * ONE_PAGE_SIZE)

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(
        accountingDao.queryPreviousAccounting(
            firstPage.last().createTime,
            ONE_PAGE_SIZE.toLong()
        )
    )
        .thenReturn(Maybe.just(secondPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f))
        .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    mainViewModel.loadFirstPage()
    val values = getStateProperty(MainState::accountingDetailList)
    mainViewModel.loadList(firstPageAdapterList, firstPage.last().createTime)

    assert(
        values.size == 3 &&
            values[1] is Loading &&
            values[2] is Success &&
            values[2]() == firstPageAdapterList.let {
          it.toMutableList().apply { addAll(secondPageAdapterList) }
        }
    )
  }

  @Test
  fun loadNextPage_whenNoMoreData_success() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val secondPage = accountings.subList(ONE_PAGE_SIZE, 2 * ONE_PAGE_SIZE - 1)
    val secondPageAdapterList = adapterList.subList(2 * ONE_PAGE_SIZE, 4 * ONE_PAGE_SIZE - 2)

    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(
        accountingDao.queryPreviousAccounting(
            firstPage.last().createTime,
            ONE_PAGE_SIZE.toLong()
        )
    )
        .thenReturn(Maybe.just(secondPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(
            R.string.main_accounting_detail_header_sum,
            100.0f))
        .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    mainViewModel.loadFirstPage()
    val values = getStateProperty(MainState::accountingDetailList)
    mainViewModel.loadList(firstPageAdapterList, firstPage.last().createTime)

    assert(
        values.size == 3 &&
            values[1] is Loading &&
            values[2] is Success &&
            values[2]() == firstPageAdapterList.let {
              it.toMutableList().apply { addAll(secondPageAdapterList) }
            }
    )

    withState(mainViewModel) {
      assert(it.isNoMoreData)
    }
  }

  @Test
  fun addOrEditAccounting_addAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val insertAccounting = Accounting(
        amount = 100.0f,
        createTime = calendar.time,
        tagName = "早餐",
        remarks = "gg"
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

    `when`(accountingDao.insertAccounting(insertAccounting)).thenReturn(addedAccounting.id.toLong())
    `when`(accountingDao.queryPreviousAccounting(calendar.time, ONE_PAGE_SIZE.toLong()))
        .thenReturn(Maybe.just(firstPage))
    `when`(accountingDao.sumOfDay(ArgumentMatchers.anyLong())).thenReturn(100.0f)
    `when`(applicationContext.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(applicationContext.getString(R.string.main_accounting_detail_header_sum, 100.0f))
    .thenReturn("共(¥100.00)")

    val initialState = MainState(lastDate = calendar.time)
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    mainViewModel.loadFirstPage()
    val data = mutableListOf<Async<List<MainAccountingDetail>>>()
    mainViewModel.selectSubscribe(owner, MainState::accountingDetailList) {
      data.add(it)
    }
    mainViewModel.addOrEditAccounting(
        accountingDetailList = firstPageAdapterList,
        accountingId = -1,
        amount = addedAccounting.amount,
        tagName = addedAccounting.tagName,
        showDate = dateTimeFormat.format(addedAccounting.createTime),
        remarks = addedAccounting.remarks)

    assert(data.size == 3 &&
        data[1] is Loading &&
        data[2] is Success &&
        data[2]() == firstPageAdapterList.let { it.toMutableList().apply { add(1, addedContent) }
    })
  }

  @Test
  fun addOrEditAccounting_addAccounting_whenNoDatas() {
    val insertAccounting = Accounting(
        amount = 100.0f,
        createTime = calendar.time,
        tagName = "早餐",
        remarks = "gg"
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
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    val values = getStateProperty(MainState::accountingDetailList)
    mainViewModel.addOrEditAccounting(
        accountingDetailList = emptyList(),
        accountingId = -1,
        amount = addedAccounting.amount,
        tagName = addedAccounting.tagName,
        showDate = dateTimeFormat.format(addedAccounting.createTime),
        remarks = addedAccounting.remarks)

    assert(values.size == 3 &&
        values[1] is Loading &&
        values[2] is Success &&
        values[2]() == listOf(addedHeader, addedContent))

    withState(mainViewModel) {
      assert(it.isNoMoreData)
      assert(!it.isNoData)
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
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    mainViewModel.loadFirstPage()
    val values = getStateProperty(MainState::accountingDetailList)
    mainViewModel.addOrEditAccounting(
        accountingDetailList = firstPageAdapterList,
        accountingId = updatedAccounting.id,
        amount = updatedAccounting.amount,
        tagName = updatedAccounting.tagName,
        showDate = dateTimeFormat.format(updatedAccounting.createTime),
        remarks = updatedAccounting.remarks)

    assert(
        values.size == 3 &&
        values[1] is Loading &&
        values[2] is Success &&
        values[2]() == firstPageAdapterList.let {
          it.toMutableList().apply { set(1, updatedAdapterItem) }
        })
  }

  @Test
  fun deleteAccounting() {
    val firstPage = accountings.take(ONE_PAGE_SIZE)
    val firstPageAdapterList = adapterList.take(2 * ONE_PAGE_SIZE)
    val afterDeletedAdapterList = firstPageAdapterList
        .subList(2, firstPageAdapterList.size)

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
    mainViewModel = MainViewModel(
        initialState,
        accountingDao,
        applicationContext)

    mainViewModel.loadFirstPage()
    val values = getStateProperty(MainState::accountingDetailList)
    mainViewModel.deleteAccounting(firstPageAdapterList, 1)

    Mockito.verify(accountingDao, Mockito.times(1)).deleteAccountingById(1)

    assert(
        values.size == 3 &&
            values[1] is Loading &&
            values[2] is Success &&
            values[2]() == afterDeletedAdapterList
    )
  }

  private fun <T> getStateProperty(prop: KProperty1<MainState, T>): MutableList<T> {
    val values = mutableListOf<T>()
    mainViewModel.selectSubscribe(owner, prop1 = prop) {
      values.add(it)
    }

    return values
  }
}
