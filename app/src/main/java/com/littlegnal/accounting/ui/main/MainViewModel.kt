package com.littlegnal.accounting.ui.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.MvRxViewModel
import com.littlegnal.accounting.base.util.toHms0
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date

class MainViewModel @AssistedInject constructor(
  @Assisted initialState: MainState,
  @Assisted stateStore: MvRxStateStore<MainState>,
  private val accountingDao: AccountingDao,
  private val applicationContext: Context
) : MvRxViewModel<MainState>(initialState, stateStore) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
      initialState: MainState,
      stateStore: MvRxStateStore<MainState>
    ): MainViewModel
  }

  init {
      loadList(lastDate = initialState.lastDate)
  }

  fun loadList(
    accountingDetailList: List<MainAccountingDetail> = emptyList(),
    lastDate: Date
  ) {
    accountingDao.queryPreviousAccounting(lastDate, ONE_PAGE_SIZE.toLong())
        .toObservable()
        .map { accountingList ->
          val newAdapterList = accountingDetailList.toMutableList()

          val isFirstPage = newAdapterList.isEmpty()
          if (isFirstPage) {
            newAdapterList.addAll(createFirstPageList(lastDate, accountingList))
//            copy(
//                error = null,
//                isLoading = false,
//                accountingDetailList = newAdapterList,
//                isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
//                isNoData = accountingList.isEmpty()
//            )
          } else {
            newAdapterList.addAll(createAccountingDetailList(lastDate, accountingList))

            newAdapterList.distinctBy {
              if (it is MainAccountingDetailContent) {
                return@distinctBy it.id.toString()
              }

              if ((it is MainAccountingDetailHeader)) {
                return@distinctBy it.title
              }

              ""
            }

//                .let {
//                  previousState.copy(
//                      error = null,
//                      isLoading = false,
//                      accountingDetailList = it,
//                      isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
//                      isNoData = false
//                  )
//                }
          }
          newAdapterList to (accountingList.size < ONE_PAGE_SIZE)
        }
        .subscribeOn(Schedulers.io())
        .execute {
          when (it) {
            is Success -> {
              val (list, isNoMoreData) = it()!!
              copy(
                error = null,
                isLoading = false,
                accountingDetailList = list,
                isNoData = list.isEmpty(),
                isNoMoreData = isNoMoreData)
            }
            is Fail -> {
              copy(error = it.error, isLoading = false)
            }
            is Loading -> {
              copy(isLoading = true)
            }
            else -> {
              copy()
            }
          }
        }
  }

  private fun createAccountingDetailList(
    lastDate: Date,
    accountingList: List<Accounting>
  ): MutableList<MainAccountingDetail> {
    var lastDateNum: Int = dateNumFormat.format(lastDate)
        .toInt()

    val detailList: MutableList<MainAccountingDetail> = mutableListOf()

    for (accounting in accountingList) {
      val createTime = accounting.createTime
      val accountingDateNum: Int = dateNumFormat.format(createTime)
          .toInt()
      if (accountingDateNum != lastDateNum) {
        detailList.add(createHeader(createTime))
        lastDateNum = accountingDateNum
      }

      detailList.add(createDetailContent(accounting))
    }

    return detailList
  }

  private fun createFirstPageList(
    lastDate: Date,
    accountingList: List<Accounting>
  ): List<MainAccountingDetail> {
    if (accountingList.isEmpty()) return listOf()
    val accountingDetailList = createAccountingDetailList(lastDate, accountingList)
    val maybeHeader: MainAccountingDetail = accountingDetailList[0]
    if (maybeHeader !is MainAccountingDetailHeader) {
      accountingDetailList.add(0, createHeader(lastDate))
    }

    return accountingDetailList
  }

  private fun createHeaderTitle(createTime: Date): String = oneDayFormat.format(createTime)

  private fun createHeader(createTime: Date): MainAccountingDetailHeader {
    val title: String = createHeaderTitle(createTime)
    val sum = accountingDao.sumOfDay(createTime.toHms0().time / 1000)
    val sumString: String = applicationContext.getString(
        R.string.main_accounting_detail_header_sum, sum
    )
    return MainAccountingDetailHeader(title, sumString)
  }

  private fun createDetailContent(accounting: Accounting): MainAccountingDetailContent {
    return MainAccountingDetailContent(
        accounting.id,
        applicationContext.getString(R.string.amount_format, accounting.amount),
        accounting.tagName,
        accounting.remarks,
        timeFormat.format(accounting.createTime),
        accounting.createTime
    )
  }

  fun addOrEditAccounting(
    accountingDetailList: List<MainAccountingDetail>,
    accountingId: Int,
    amount: Float,
    tagName: String,
    showDate: String,
    remarks: String?
  ) {
    if (accountingId == -1) {
      addAccounting(accountingDetailList, amount, tagName, showDate, remarks)
    } else {
      updateAccounting(accountingDetailList, accountingId, amount, tagName, showDate, remarks)
    }
  }

  private fun addAccounting(
    accountingDetailList: List<MainAccountingDetail>,
    amount: Float,
    tagName: String,
    showDate: String,
    remarks: String?
  ) {
    Observable.fromCallable {
      val accounting = Accounting(
          amount,
          dateTimeFormat.parse(showDate),
          tagName,
          remarks
      )
      val insertedId = accountingDao.insertAccounting(accounting)
      accounting.id = insertedId.toInt()
      val newContent = createDetailContent(accounting)
      val newAccountingList = accountingDetailList.toMutableList()
      if (newAccountingList.isEmpty()) {
        newAccountingList.add(createHeader(accounting.createTime))
        newAccountingList.add(newContent)
      } else {
        val title: String = createHeaderTitle(accounting.createTime)
        val existHeaderIndex = newAccountingList.indexOfFirst {
          it is MainAccountingDetailHeader && it.title == title
        }
        if (existHeaderIndex != -1) {
          var insertContentIndex = -1
          for (i in existHeaderIndex + 1 until newAccountingList.size) {
            val tempContent = newAccountingList[i]
            if ((tempContent is MainAccountingDetailContent &&
                    tempContent.createTime <= accounting.createTime) ||
                tempContent is MainAccountingDetailHeader) {
              insertContentIndex = i
              break
            }
          }

          if (insertContentIndex != -1) {
            val addedHeader = createHeader(accounting.createTime)
            newAccountingList[existHeaderIndex] = addedHeader
            newAccountingList.add(insertContentIndex, newContent)
          }
        } else {
          val newHeaderIndex = newAccountingList.indexOfFirst {
            it is MainAccountingDetailHeader && it.title < title
          }

          if (newHeaderIndex != -1) {
            val addedHeader = createHeader(accounting.createTime)
              newAccountingList.add(newHeaderIndex, newContent)
              newAccountingList.add(newHeaderIndex, addedHeader)
          }
        }
      }
      newAccountingList
    }
    .subscribeOn(Schedulers.io())
    .execute {
      when (it) {
        is Loading -> {
          copy(error = null, isLoading = true)
        }
        is Fail -> {
          copy(error = it.error, isLoading = false)
        }
        is Success -> {
          val list = it()!!
          copy(
              accountingDetailList = list,
              isLoading = false,
              error = null,
              isNoData = list.isEmpty(),
              isNoMoreData = list.size < ONE_PAGE_SIZE)
        }
        else -> { copy() }
      }
    }
  }

  private fun updateAccounting(
    accountingDetailList: List<MainAccountingDetail>,
    accountingId: Int,
    amount: Float,
    tagName: String,
    showDate: String,
    remarks: String?
  ) {
    Observable.fromCallable {
      val accounting = Accounting(
          amount,
          dateTimeFormat.parse(showDate),
          tagName,
          remarks
      ).apply { id = accountingId }
      accountingDao.insertAccounting(accounting)
      val newContent = createDetailContent(accounting)
      val newAccountingList = accountingDetailList.toMutableList()
      val oldContent = newAccountingList
          .filter { it is MainAccountingDetailContent }
          .map { it as MainAccountingDetailContent }
          .find { it.id == newContent.id }

      oldContent?.apply {
        newAccountingList.indexOf(oldContent)
            .apply {
              findAndUpdateHeader(newAccountingList, this)
              newAccountingList[this] = newContent
            }
      }
      newAccountingList
    }
    .subscribeOn(Schedulers.io())
    .execute {
      when (it) {
        is Loading -> {
          copy(isLoading = true, error = null)
        }
        is Fail -> {
          copy(isLoading = false, error = it.error)
        }
        is Success -> {
          val list = it()!!
          copy(
              isLoading = false,
              error = null,
              accountingDetailList = list,
              isNoData = list.isEmpty(),
              isNoMoreData = list.size < ONE_PAGE_SIZE)
        }
        else -> { copy() }
      }
    }
  }

  fun deleteAccounting(accountingDetailList: List<MainAccountingDetail>, deletedId: Int) {
    Observable.fromCallable {
      accountingDao.deleteAccountingById(deletedId)

      val newAccountingList = accountingDetailList.toMutableList()

      val deleteContentIndex: Int = newAccountingList.indexOfLast {
        it is MainAccountingDetailContent && it.id == deletedId
      }

      // 当天只有一条数据的时候把头部也删掉
      var headerIndex = -1
      if (deleteContentIndex > 0 &&
          newAccountingList[deleteContentIndex - 1] is MainAccountingDetailHeader
      ) {
        headerIndex = deleteContentIndex - 1
      }

      var nextHeaderIndex = -1
      if (deleteContentIndex + 1 <= newAccountingList.size - 1 &&
          newAccountingList[deleteContentIndex + 1] is MainAccountingDetailHeader
      ) {
        nextHeaderIndex = deleteContentIndex + 1
      }

      if (((nextHeaderIndex == -1 && deleteContentIndex == newAccountingList.size - 1) &&
              headerIndex != -1) ||
          (headerIndex != -1 && nextHeaderIndex != -1)
      ) {
        newAccountingList.removeAt(deleteContentIndex)
        newAccountingList.removeAt(headerIndex)
      } else {
        findAndUpdateHeader(newAccountingList, deleteContentIndex)
        newAccountingList.removeAt(deleteContentIndex)
      }

      newAccountingList
    }
    .subscribeOn(Schedulers.io())
    .execute {
      when (it) {
        is Loading -> {
          copy(error = null, isLoading = true)
        }
        is Success -> {
          val list = it()!!
          copy(
              error = null,
              isLoading = false,
              accountingDetailList = list,
              isNoData = list.isEmpty(),
              isNoMoreData = list.size < ONE_PAGE_SIZE)
        }
        is Fail -> {
          copy(error = it.error, isLoading = false)
        }
        else -> { copy() }
      }
    }
  }

  private fun findAndUpdateHeader(
    list: MutableList<MainAccountingDetail>,
    addOrUpdateIndex: Int
  ) {
    list.indexOfLast { it is MainAccountingDetailHeader && list.indexOf(it) < addOrUpdateIndex }
        .apply {
          val createTime = (list[addOrUpdateIndex] as MainAccountingDetailContent).createTime
          val sum = accountingDao.sumOfDay(createTime.toHms0().time / 1000)
          val sumString: String = applicationContext.getString(
              R.string.main_accounting_detail_header_sum, sum
          )
          list[this] = (list[this] as MainAccountingDetailHeader).copy(total = sumString)
        }
  }

  companion object : MvRxViewModelFactory<MainState> {

    const val ONE_PAGE_SIZE = 15

    @SuppressLint("SimpleDateFormat")
    private val dateNumFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")

    @VisibleForTesting
    @SuppressLint("SimpleDateFormat")
    val oneDayFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @VisibleForTesting
    @SuppressLint("SimpleDateFormat")
    val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")

    @VisibleForTesting
    @SuppressLint("SimpleDateFormat")
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

    @JvmStatic override fun create(
      activity: FragmentActivity,
      state: MainState
    ): BaseMvRxViewModel<MainState> {
      return (activity as MainActivity).mainViewModelFactory
          .create(state, RealMvRxStateStore(state))
    }
  }
}
