package com.littlegnal.accounting.ui.main

import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import io.reactivex.Maybe
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author littlegnal
 * @date 2017/10/22
 */
class MainViewPresenterTest {

  @Mock
  private lateinit var accountingDao: AccountingDao

  @Mock
  private lateinit var context: Context

  private lateinit var mainPresenter: MainPresenter

  private lateinit var addOrUpdatePublisher: PublishSubject<Accounting>

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    addOrUpdatePublisher = PublishSubject.create()
    mainPresenter = MainPresenter(context, accountingDao, addOrUpdatePublisher)
  }

  @Test
  fun test_loadFirstPage_whenNoDatas() {
    `when`(accountingDao.queryPreviousAccounting(mainPresenter.NOW.time, 15))
        .thenReturn(Maybe.just(listOf()))

    val mainTestImpl = MainViewTestImpl(mainPresenter)
    mainTestImpl.fireLoadFirstPageIntent()

    val today = mainPresenter.NOW.time //Calendar.getInstance().time
    val loadingState = MainViewState(lastDate = today, isLoading = true)
    val firstPageDataState = MainViewState(lastDate = today, isNoData = true)
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState)
  }

  @Test
  fun test_loadFirstPage() {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR, -1)
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")

    `when`(context.getString(R.string.amount_format, account1.amount)).thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")
    `when`(accountingDao.queryPreviousAccounting(mainPresenter.NOW.time, 15))
        .thenReturn(Maybe.just(listOf(account1)))
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(calendar.time))).thenReturn(100.0f)

    val mainTestImpl = MainViewTestImpl(mainPresenter)
    mainTestImpl.fireLoadFirstPageIntent()

    val header = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(calendar.time),
        "共(¥100.00)")

    val content = MainAccountingDetailContent(
        account1.id,
        "¥100.00",
        account1.tagName,
        account1.remarks,
        TIME_FORMAT.format(account1.createTime),
        account1.createTime)

    val loadingState = MainViewState(lastDate = mainPresenter.NOW.time, isLoading = true)
    val firstPageDataState = MainViewState(
        lastDate = calendar.time,
        accountingDetailList = listOf(header, content),
        isLoading = false,
        isNoData = false,
        isNoMoreData = true)

    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState)
  }

  @Test
  fun test_loadNextPage() {
    var id = 1
    val calendar = Calendar.getInstance()
    val firstPageAccountingList = mutableListOf<Accounting>()
    val firstPageDetailContentList = mutableListOf<MainAccountingDetail>()
    for (i in 1..15) {
      val account = Accounting(
          100.0f,
          calendar.time,
          "早餐",
          "100块的茶叶蛋")
      account.id = id

      val content = MainAccountingDetailContent(
          account.id,
          "¥100.00",
          account.tagName,
          account.remarks,
          TIME_FORMAT.format(account.createTime),
          account.createTime)
      calendar.add(Calendar.SECOND, -1)
      firstPageAccountingList.add(account)
      firstPageDetailContentList.add(content)
      ++id
    }
    val header = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(firstPageAccountingList.first().createTime),
        "共(¥1700.00)")
    firstPageDetailContentList.add(0, header)
    val firstPageLastDate = firstPageAccountingList.last().createTime

    val nextPageAccountingList = mutableListOf<Accounting>()
    val nextPageDetailList = mutableListOf<MainAccountingDetail>()
    val nextPageCalendar = Calendar.getInstance().apply { time = calendar.time }
    for (i in 1..2) {
      val account = Accounting(
          100.0f,
          nextPageCalendar.time,
          "早餐",
          "100块的茶叶蛋")
      account.id = id
      val content = MainAccountingDetailContent(
          account.id,
          "¥100.00",
          account.tagName,
          account.remarks,
          TIME_FORMAT.format(account.createTime),
          account.createTime)
      nextPageCalendar.add(Calendar.SECOND, -1)
      nextPageAccountingList.add(account)
      nextPageDetailList.add(content)
      ++id
    }
    val nextPageLastDate = nextPageAccountingList.last().createTime

    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f * 17))
        .thenReturn("共(¥1700.00)")
    `when`(accountingDao.queryPreviousAccounting(mainPresenter.NOW.time, 15))
        .thenReturn(Maybe.just(firstPageAccountingList.toList()))
    `when`(accountingDao.queryPreviousAccounting(firstPageLastDate, 15))
        .thenReturn(Maybe.just(nextPageAccountingList.toList()))
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(calendar.time))).thenReturn(1700.0f)

    val firstPageLoadingState = MainViewState(lastDate = mainPresenter.NOW.time, isLoading = true)
    val firstPageDataState = MainViewState(
        lastDate = firstPageLastDate,
        accountingDetailList = firstPageDetailContentList.toList(),
        isLoading = false,
        isNoData = false,
        isNoMoreData = false)

    val nextPageLoadingState = firstPageDataState.copy(isLoading = true)

    val tempList = firstPageDetailContentList.toMutableList()
        .apply { this.addAll(nextPageDetailList) }

    val nextPageDataState = MainViewState(
        lastDate = nextPageLastDate,
        accountingDetailList = tempList,
        isLoading = false,
        isNoData = false,
        isNoMoreData = true)

    val mainTestImpl = MainViewTestImpl(mainPresenter)
    mainTestImpl.fireLoadFirstPageIntent()
    mainTestImpl.fireLoadNextPageIntent(firstPageLastDate)
    mainTestImpl.assertViewStateRendered(
        firstPageLoadingState,
        firstPageDataState,
        nextPageLoadingState,
        nextPageDataState)
  }

  @Test
  fun test_addAccounting_whenNoDatas() {
    `when`(accountingDao.queryPreviousAccounting(Calendar.getInstance().time, 15))
        .thenReturn(Maybe.just(listOf()))

    val mainTestImpl = MainViewTestImpl(mainPresenter)

    val today = mainPresenter.NOW.time //Calendar.getInstance().time
    val loadingState = MainViewState(lastDate = today, isLoading = true)
    val firstPageDataState = MainViewState(lastDate = today, isNoData = true)

    mainTestImpl.fireLoadFirstPageIntent()
    mainTestImpl.assertViewStateRendered(
        loadingState,
        firstPageDataState)

    val tempCalendar = Calendar.getInstance().apply { this.add(Calendar.HOUR, -1) }
    val account = Accounting(
        100.0f,
        tempCalendar.time,
        "早餐",
        "100块的茶叶蛋")

    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(100.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val header = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account.createTime),
        "共(¥100.00)")

    val content = MainAccountingDetailContent(
        account.id,
        "¥100.00",
        account.tagName,
        account.remarks,
        TIME_FORMAT.format(account.createTime),
        account.createTime)

    val addAccountingState = MainViewState(
        lastDate = content.createTime,
        accountingDetailList = listOf(header, content))

    addOrUpdatePublisher.onNext(account)

    mainTestImpl.assertViewStateRendered(
        loadingState,
        firstPageDataState,
        addAccountingState)
  }

  @Test
  fun test_addAccounting() {
    val mainTestImpl = MainViewTestImpl(mainPresenter)

    val today = mainPresenter.NOW.time //Calendar.getInstance().time

    val tempCalendar = Calendar.getInstance().apply { this.add(Calendar.HOUR, -1) }
    val account = Accounting(
        100.0f,
        tempCalendar.time,
        "早餐",
        "100块的茶叶蛋")

    `when`(accountingDao.queryPreviousAccounting(today, 15))
        .thenReturn(Maybe.just(listOf(account)))
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(100.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val header = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account.createTime),
        "共(¥100.00)")

    val content = MainAccountingDetailContent(
        account.id,
        "¥100.00",
        account.tagName,
        account.remarks,
        TIME_FORMAT.format(account.createTime),
        account.createTime)

    val loadingState = MainViewState(lastDate = today, isLoading = true)
    val firstPageDataState = MainViewState(
        lastDate = tempCalendar.time,
        accountingDetailList = listOf(header, content),
        isNoMoreData = true)

    mainTestImpl.fireLoadFirstPageIntent()
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState)

    tempCalendar.add(Calendar.HOUR, -1)
    val account2 = Accounting(
        100.0f,
        tempCalendar.time,
        "早餐",
        "100块的茶叶蛋")
    account2.id = 1

    val content2 = MainAccountingDetailContent(
        account2.id,
        "¥100.00",
        account2.tagName,
        account2.remarks,
        TIME_FORMAT.format(account2.createTime),
        account2.createTime)

    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(200.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 200.0f))
        .thenReturn("共(¥200.00)")
    val addState = MainViewState(
        lastDate = tempCalendar.time,
        accountingDetailList = listOf(header, content, content2),
        isNoMoreData = true)
    addOrUpdatePublisher.onNext(account2)

    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState, addState)

    val account3Calendar = Calendar.getInstance().apply { time = tempCalendar.time }
    account3Calendar.add(Calendar.MINUTE, 1)
    val account3 = Accounting(
        100.0f,
        account3Calendar.time,
        "早餐",
        "100块的茶叶蛋")
    account3.id = 2
    val content3 = MainAccountingDetailContent(
        account3.id,
        "¥100.00",
        account3.tagName,
        account3.remarks,
        TIME_FORMAT.format(account3.createTime),
        account3.createTime)

    val addState2 = MainViewState(
        lastDate = tempCalendar.time,
        accountingDetailList = listOf(header, content, content3, content2),
        isNoMoreData = true)

    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(300.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 300.0f))
        .thenReturn("共(¥300.00)")

    addOrUpdatePublisher.onNext(account3)
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState, addState, addState2)
  }

  @Test
  fun test_updateAccounting() {
    val mainTestImpl = MainViewTestImpl(mainPresenter)

    val today = mainPresenter.NOW.time //Calendar.getInstance().time

    val tempCalendar = Calendar.getInstance().apply { this.add(Calendar.HOUR, -1) }
    val account = Accounting(
        100.0f,
        tempCalendar.time,
        "早餐",
        "100块的茶叶蛋")
    account.id = 1

    `when`(accountingDao.queryPreviousAccounting(today, 15))
        .thenReturn(Maybe.just(listOf(account)))
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(100.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val header = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account.createTime),
        "共(¥100.00)")

    val content = MainAccountingDetailContent(
        account.id,
        "¥100.00",
        account.tagName,
        account.remarks,
        TIME_FORMAT.format(account.createTime),
        account.createTime)

    val loadingState = MainViewState(lastDate = today, isLoading = true)
    val firstPageDataState = MainViewState(
        lastDate = tempCalendar.time,
        accountingDetailList = listOf(header, content),
        isNoMoreData = true)

    mainTestImpl.fireLoadFirstPageIntent()
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState)

    val updateAccount = Accounting(
        200.0f,
        tempCalendar.time,
        "早餐",
        "100块的茶叶蛋")
    updateAccount.id = 1

    val updateContent = MainAccountingDetailContent(
        updateAccount.id,
        "¥200.00",
        updateAccount.tagName,
        updateAccount.remarks,
        TIME_FORMAT.format(updateAccount.createTime),
        updateAccount.createTime)
    val updatedHeader = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account.createTime),
        "共(¥200.00)")

    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(tempCalendar.time)))
        .thenReturn(200.0f)
    `when`(context.getString(R.string.amount_format, 200.0f))
        .thenReturn("¥200.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 200.0f))
        .thenReturn("共(¥200.00)")

    val updateState = MainViewState(
        lastDate = tempCalendar.time,
        accountingDetailList = listOf(updatedHeader, updateContent),
        isNoMoreData = true)

    addOrUpdatePublisher.onNext(updateAccount)
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState, updateState)
  }

  @Test
  fun test_deleteAccounting() {
    val calendar = Calendar.getInstance()
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    account1.id = 1
    calendar.add(Calendar.HOUR, -1)
    val account2 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    account2.id = 2
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val account3 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    account3.id = 3

    val header1 = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account1.createTime),
        "共(¥200.00)")

    val content1 = MainAccountingDetailContent(
        account1.id,
        "¥100.00",
        account1.tagName,
        account1.remarks,
        TIME_FORMAT.format(account1.createTime),
        account1.createTime)
    val content2 = MainAccountingDetailContent(
        account2.id,
        "¥100.00",
        account2.tagName,
        account2.remarks,
        TIME_FORMAT.format(account2.createTime),
        account2.createTime)

    val header2 = MainAccountingDetailHeader(
        ONE_DAY_FORMAT.format(account3.createTime),
        "共(¥100.00)")
    val content3 = MainAccountingDetailContent(
        account3.id,
        "¥100.00",
        account3.tagName,
        account3.remarks,
        TIME_FORMAT.format(account3.createTime),
        account3.createTime)

    `when`(accountingDao.queryPreviousAccounting(mainPresenter.NOW.time, 15))
        .thenReturn(Maybe.just(listOf(account1, account2, account3)))
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(account1.createTime)))
        .thenReturn(200.0f)
    `when`(accountingDao.sumOfDay(ONE_DAY_FORMAT.format(account3.createTime)))
        .thenReturn(100.0f)
    `when`(context.getString(R.string.amount_format, 100.0f))
        .thenReturn("¥100.00")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 200.0f))
        .thenReturn("共(¥200.00)")
    `when`(context.getString(R.string.main_accounting_detail_header_sum, 100.0f))
        .thenReturn("共(¥100.00)")

    val loadingState = MainViewState(lastDate = mainPresenter.NOW.time, isLoading = true)
    val firstPageDataState = MainViewState(
        lastDate = account3.createTime,
        accountingDetailList = listOf(header1, content1, content2, header2, content3),
        isNoMoreData = true)

    val mainTestImpl = MainViewTestImpl(mainPresenter)
    mainTestImpl.fireLoadFirstPageIntent()
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState)

    // delete account2
    val deleteState1 = MainViewState(
        lastDate = account3.createTime,
        accountingDetailList = listOf(header1, content2, header2, content3),
        isNoMoreData = true)

    mainTestImpl.fireDeleteAccountingIntent(1)
    mainTestImpl.assertViewStateRendered(loadingState, firstPageDataState, deleteState1)

    val deleteState2 = MainViewState(
        lastDate = account2.createTime,
        accountingDetailList = listOf(header1, content2),
        isNoMoreData = true)

    mainTestImpl.fireDeleteAccountingIntent(3)
    mainTestImpl.assertViewStateRendered(
        loadingState,
        firstPageDataState,
        deleteState1,
        deleteState2)
  }

  companion object {

    @BeforeClass @JvmStatic fun setUpRx() {
      RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @AfterClass @JvmStatic fun resetRx() {
      RxAndroidPlugins.reset()
    }

    private val ONE_DAY_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    private val TIME_FORMAT: SimpleDateFormat = SimpleDateFormat("HH:mm")
  }

}