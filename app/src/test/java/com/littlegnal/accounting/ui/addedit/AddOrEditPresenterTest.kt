package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Maybe
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author littlegnal
 * @date 2017/10/24
 */
class AddOrEditPresenterTest {

  @Mock
  private lateinit var accountingDao: AccountingDao

  private lateinit var addOrEditPresenter: AddOrEditPresenter

  private lateinit var addOrUpdatePublisher: PublishSubject<Accounting>

  @Mock
  private lateinit var addOrEditNavigator: AddOrEditNavigator

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    addOrUpdatePublisher = PublishSubject.create()
    addOrEditPresenter = AddOrEditPresenter(
        accountingDao,
        addOrUpdatePublisher,
        addOrEditNavigator)
  }

  @Test
  fun test_loadDataIntent_add() {
    val addOrEditViewImpl = AddOrEditViewTestImpl(addOrEditPresenter)
    val addState = AddOrEditViewState(
        dateTime = DATE_TIME_FORMAT.format(addOrEditPresenter.NOW.time))
    addOrEditViewImpl.fireLoadDataIntent(AddOrEditActivity.ADD)
    addOrEditViewImpl.assertViewStateRendered(addState)
  }

  @Test
  fun test_loadDataIntent_update() {
    val calendar = Calendar.getInstance()
    val account = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    account.id = 1

    `when`(accountingDao.getAccountingById(1)).thenReturn(Maybe.just(account))

    val addOrEditViewImpl = AddOrEditViewTestImpl(addOrEditPresenter)
    val updateState = AddOrEditViewState(
        account.amount.toString(),
        account.tagName,
        DATE_TIME_FORMAT.format(account.createTime),
        account.remarks)
    addOrEditViewImpl.fireLoadDataIntent(1)
    addOrEditViewImpl.assertViewStateRendered(updateState)
  }

  @Test
  fun test_saveOrUpdateIntent_add() {
    val calendar = Calendar.getInstance()
    val account = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    val stringArray: Array<String> = arrayOf(
        AddOrEditActivity.ADD.toString(),
        account.amount.toString(),
        account.tagName,
        DATE_TIME_FORMAT.format(account.createTime),
        account.remarks!!)

    `when`(accountingDao.insertAccounting(account)).thenReturn(1)
    val addOrEditViewImpl = AddOrEditViewTestImpl(addOrEditPresenter)

    val insertAccount = account.copy().apply { id = 1 }
    addOrEditViewImpl.fireSaveOrUpdateIntent(stringArray)

    verify(addOrEditNavigator, times(1)).finish()
  }

  companion object {

    private val DATE_TIME_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm")

    @BeforeClass
    @JvmStatic fun setUpRx() {
      RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @AfterClass
    @JvmStatic fun resetRx() {
      RxAndroidPlugins.reset()
    }
  }
}