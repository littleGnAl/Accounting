package com.littlegnal.accounting.ui.addedit

import androidx.lifecycle.Lifecycle
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.base.RxImmediateSchedulerRule
import com.littlegnal.accounting.base.TestLifecycleOwner
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
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
        amount = 100.0f,
        createTime = calendar.time,
        tagName = "早餐",
        remarks = "100块的茶叶蛋"
    ).apply { id = 1 }

    Mockito.`when`(accountingDao.getAccountingById(1))
        .thenReturn(Maybe.just(account))

    val initialState = AddOrEditMvRxViewState(id = 1)
    addOrEditViewModel = AddOrEditViewModel(initialState, accountingDao)

    val data = mutableListOf<Async<Accounting>>()
    addOrEditViewModel.selectSubscribe(owner, AddOrEditMvRxViewState::accounting) {
      data.add(it)
    }
    addOrEditViewModel.loadAccounting(1)
    assert(data.size == 3 && data[1] is Loading && data[2] is Success && data[2]() == account)
  }

  @Test
  fun loadAccounting_add() {
    val initialState = AddOrEditMvRxViewState()
    addOrEditViewModel = AddOrEditViewModel(initialState, accountingDao)
    addOrEditViewModel.loadAccounting(-1)
    withState(addOrEditViewModel) {
      assert(it.id == -1 && it.accounting is Uninitialized)
    }
  }
}
