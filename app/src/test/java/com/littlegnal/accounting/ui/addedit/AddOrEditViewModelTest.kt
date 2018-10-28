package com.littlegnal.accounting.ui.addedit

import androidx.lifecycle.Lifecycle
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
import com.littlegnal.accounting.base.TestMvRxStateStore
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.addedit.AddOrEditViewModel.Companion.dateTimeFormat
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.Calendar

class AddOrEditViewModelTest {

  @Rule
  @JvmField
  val scheduler = RxImmediateSchedulerRule()

  private lateinit var addOrEditViewModel: AddOrEditViewModel

  private lateinit var owner: TestLifecycleOwner

  private lateinit var testMvRxStateStore: TestMvRxStateStore<AddOrEditMvRxViewState>

  private fun initViewModel(initialState: AddOrEditMvRxViewState) {
    testMvRxStateStore = TestMvRxStateStore.create(initialState)
    addOrEditViewModel = AddOrEditViewModel(initialState, testMvRxStateStore, accountingDao)
  }

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    owner = TestLifecycleOwner()
    owner.lifecycle.markState(Lifecycle.State.STARTED)
  }

  @Mock
  private lateinit var accountingDao: AccountingDao

  @Test
  fun loadAccounting_edit() {
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

    val loadingState = AddOrEditMvRxViewState(id = 1, isLoading = true, error = null)
    val updateState = loadingState.copy(
        isLoading = false,
        amount = account.amount.toString(),
        tagName = account.tagName,
        dateTime = dateTimeFormat.format(account.createTime),
        remarks = account.remarks
    )

    Mockito.`when`(accountingDao.getAccountingById(1))
        .thenReturn(Maybe.just(account))

    val initialState = AddOrEditMvRxViewState(id = 1)
    initViewModel(initialState)

    testMvRxStateStore.testAllStates { stateList ->
      stateList.size == 3 && stateList[2] == updateState
    }
  }

  @Test
  fun loadAccounting_add() {
    val initialState = AddOrEditMvRxViewState()
    initViewModel(initialState)
    testMvRxStateStore.testAllStates { stateList ->
      stateList.size == 1 && stateList[0] == initialState
    }
  }
}
