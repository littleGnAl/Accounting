package com.littlegnal.accounting.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.VisibleForTesting
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.littlegnal.accounting.R
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/8/7
 */
class MainPresenter @Inject constructor(
    private var applicationContext: Context,
    private var accountingDao: AccountingDao,
    private var addOrUpdateObservable: PublishSubject<Accounting>) :
    MviBasePresenter<MainView, MainViewState>() {

  @SuppressLint("SimpleDateFormat")
  private val dateNumFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
  @SuppressLint("SimpleDateFormat")
  private val oneDayFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
  @SuppressLint("SimpleDateFormat")
  private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")

  @VisibleForTesting
  val NOW: Calendar = Calendar.getInstance()

  private val preDetailList: MutableList<Accounting> = mutableListOf()

  override fun bindIntents() {
    val loadDataIntent: Observable<MainPartialStateChanges> =
        intent(MainView::loadFirstPageIntent)
            .doOnNext { Timber.d("loadFirstPageIntent")}
            .flatMap {
              accountingDao.queryPreviousAccounting(NOW.time, 15)
                  .toObservable()
                  .doOnNext { preDetailList.addAll(it) }
                  .map<MainPartialStateChanges> {
                    MainPartialStateChanges.LoadFirstPagePartialState(it)
                  }
                  .onErrorReturn { MainPartialStateChanges.ErrorPartialState(it.message) }
                  .subscribeOn(Schedulers.io())
            }

    val loadNextPageIntent: Observable<MainPartialStateChanges> =
        intent(MainView::loadNextPageIntent)
            .doOnNext{ Timber.d("loadNextPageIntent") }
            .flatMap { lastDate: Date ->
              accountingDao.queryPreviousAccounting(lastDate, 15)
                  .toObservable()
                  .doOnNext { preDetailList.addAll(it) }
                  .map<MainPartialStateChanges> {
                    MainPartialStateChanges.LoadNextPagePartialState(lastDate, it)
                  }
                  .delay(2, TimeUnit.SECONDS) // 特意延时2秒，作为demo使加载效果更明显
                  .startWith(MainPartialStateChanges.LoadingPartialState)
                  .subscribeOn(Schedulers.io())
            }

    val addOrUpdateIntent: Observable<MainPartialStateChanges> =
        addOrUpdateObservable
            .doOnNext { Timber.d("addOrUpdateIntent") }
            .map { MainPartialStateChanges.AddOrUpdatePartialState(it) }

    val deleteAccountingIntent: Observable<MainPartialStateChanges> =
        intent(MainView::deleteAccountingIntent)
            .doOnNext { Timber.d("deleteAccountingIntent") }
            .flatMap { deletedId ->
              Observable.fromCallable { accountingDao.deleteAccountingById(deletedId) }
                  .map {
                    MainPartialStateChanges.DeleteAccountingPartialState(deletedId)
                  }
                  .subscribeOn(Schedulers.io())
            }

    val allIntent = Observable.merge(
        loadDataIntent,
        loadNextPageIntent,
        addOrUpdateIntent,
        deleteAccountingIntent)

    val stateIntents: Observable<MainViewState> =
        allIntent.distinctUntilChanged()
            .scan(MainViewState(lastDate = NOW.time, isLoading = true), this::viewStateReducer)
            .observeOn(AndroidSchedulers.mainThread())

    subscribeViewState(stateIntents, MainView::render)
  }

  private fun viewStateReducer(
      preViewState: MainViewState,
      partialChanges: MainPartialStateChanges): MainViewState {
    return when (partialChanges) {
      is MainPartialStateChanges.LoadFirstPagePartialState -> {
        createLoadFirstPageState(preViewState, partialChanges)
      }

      is MainPartialStateChanges.LoadNextPagePartialState -> {
        createLoadNextPageState(preViewState, partialChanges)
      }

      is MainPartialStateChanges.AddOrUpdatePartialState -> {
        createAddOrUpdateState(preViewState, partialChanges)
      }

      is MainPartialStateChanges.DeleteAccountingPartialState -> {
        createDeleteAccountingState(preViewState, partialChanges)
      }

      is MainPartialStateChanges.LoadingPartialState -> {
        preViewState.copy(error = null, isLoading = true)
      }

      is MainPartialStateChanges.ErrorPartialState -> {
        preViewState.copy(error = partialChanges.error)
      }
    }

  }

  private fun createLoadFirstPageState(
      preViewState: MainViewState,
      partialChanges: MainPartialStateChanges.LoadFirstPagePartialState
  ): MainViewState {
    val accountingList = partialChanges.accountingList
    if (accountingList.isEmpty())
      return preViewState.copy(error = null, isNoData = true, isLoading = false)
    val lastDate: Date = accountingList.last().createTime
    return preViewState.copy(
        lastDate = lastDate,
        accountingDetailList = createFirstPageList(partialChanges.accountingList),
        error = null,
        isNoMoreData = accountingList.size < 15,
        isLoading = false)
  }

  private fun createAddOrUpdateState(
      preViewState: MainViewState,
      partialChanges: MainPartialStateChanges.AddOrUpdatePartialState
  ): MainViewState {
    val addOrEditAccounting = partialChanges.accounting
    val newContent = createDetailContent(addOrEditAccounting)
    val newAccountingList = preViewState.accountingDetailList.toMutableList()
    if (newAccountingList.isEmpty()) {
      newAccountingList.add(createHeader(addOrEditAccounting.createTime))
      newAccountingList.add(newContent)
      return preViewState.copy(
          lastDate = (newAccountingList.last() as MainAccountingDetailContent).createTime,
          accountingDetailList = newAccountingList,
          error = null,
          isNoData = false,
          isLoading = false)
    }

    val oldContent = newAccountingList
        .filter { it is MainAccountingDetailContent }
        .map { it as MainAccountingDetailContent }
        .find { it.id == newContent.id }

    // update
    oldContent?.apply {
      newAccountingList.indexOf(oldContent).apply {
        findAndUpdateHeader(newAccountingList, this)
        newAccountingList[this] = newContent
      }
    } ?: apply {
      // add
      val title: String = createHeaderTitle(addOrEditAccounting.createTime)
      val header: MainAccountingDetailHeader =
          newAccountingList[0] as MainAccountingDetailHeader

      newAccountingList.indexOfFirst {
        it is MainAccountingDetailContent && it.createTime <= addOrEditAccounting.createTime
      }.apply {
        // index为-1时表示找到了最后，所以下标直接赋值为newAccountingList.size
        val insertIndex = if (this == -1) newAccountingList.size else this
        if (title == header.title) {
          newAccountingList.add(insertIndex, newContent)
        } else {
          val addedHeader = createHeader(addOrEditAccounting.createTime)
          newAccountingList.add(insertIndex, newContent)
          newAccountingList.add(insertIndex, addedHeader)
        }
      }
    }

    return preViewState.copy(
        lastDate = (newAccountingList.last() as MainAccountingDetailContent).createTime,
        error = null,
        accountingDetailList = newAccountingList,
        isLoading = false)
  }

  private fun createLoadNextPageState(
      preViewState: MainViewState,
      partialChanges: MainPartialStateChanges.LoadNextPagePartialState
  ): MainViewState {
    if (partialChanges.accountingList.isEmpty())
      return preViewState.copy(error = null, isNoMoreData = true, isLoading = false)
    val accountingList = createAccountingDetailList(
        partialChanges.lastDate,
        partialChanges.accountingList)

    val newAccountingList = preViewState.accountingDetailList.toMutableList()
    val isNoMoreData = partialChanges.accountingList.size < 15
    newAccountingList.addAll(accountingList)

    val distinctList = newAccountingList.distinctBy {
      if (it is MainAccountingDetailContent) {
        return@distinctBy it.id.toString()
      }

      if ((it is MainAccountingDetailHeader)) {
        return@distinctBy it.title
      }

      ""
    }

    return preViewState.copy(
        lastDate = partialChanges.accountingList.last().createTime,
        accountingDetailList = distinctList,
        error = null,
        isNoMoreData = isNoMoreData,
        isLoading = false)
  }

  private fun createDeleteAccountingState(
      preViewState: MainViewState,
      partialChanges: MainPartialStateChanges.DeleteAccountingPartialState
  ): MainViewState {
    val newAccountingList = preViewState.accountingDetailList.toMutableList()

    val deleteContentIndex: Int = newAccountingList.indexOfLast {
      it is MainAccountingDetailContent && it.id == partialChanges.deletedId
    }

    // 当天只有一条数据的时候把头部也删掉
    var headerIndex = -1
    if (deleteContentIndex > 0 &&
        newAccountingList[deleteContentIndex - 1] is MainAccountingDetailHeader) {
      headerIndex = deleteContentIndex - 1
    }

    var nextHeaderIndex = -1
    if (deleteContentIndex + 1 <= newAccountingList.size - 1 &&
        newAccountingList[deleteContentIndex + 1] is MainAccountingDetailHeader) {
      nextHeaderIndex = deleteContentIndex + 1
    }

    if (((nextHeaderIndex == -1 && deleteContentIndex == newAccountingList.size - 1)
        && headerIndex != -1)
        || (headerIndex != -1 && nextHeaderIndex != -1)) {
      newAccountingList.removeAt(deleteContentIndex)
      newAccountingList.removeAt(headerIndex)
    } else {
      findAndUpdateHeader(newAccountingList, deleteContentIndex)
      newAccountingList.removeAt(deleteContentIndex)
    }

    return preViewState.copy(
        lastDate = (newAccountingList.last() as MainAccountingDetailContent).createTime,
        accountingDetailList = newAccountingList,
        error = null,
        isNoData = newAccountingList.isEmpty(),
        isLoading = false)
  }


  private fun createAccountingDetailList(
      lastDate: Date,
      accountingList: List<Accounting>): MutableList<MainAccountingDetail> {
    var lastDateNum: Int = dateNumFormat.format(lastDate).toInt()

    val detailList: MutableList<MainAccountingDetail> = mutableListOf()

    for (accounting in accountingList) {
      val createTime = accounting.createTime
      val accountingDateNum: Int = dateNumFormat.format(createTime).toInt()
      if (accountingDateNum != lastDateNum) {
        detailList.add(createHeader(createTime))
        lastDateNum = accountingDateNum
      }

      detailList.add(createDetailContent(accounting))
    }

    return detailList
  }

  private fun createFirstPageList(accountingList: List<Accounting>): List<MainAccountingDetail> {
    val accountingDetailList = createAccountingDetailList(NOW.time, accountingList)
    val maybeHeader: MainAccountingDetail = accountingDetailList[0]
    if (maybeHeader !is MainAccountingDetailHeader) {
      accountingDetailList.add(0, createHeader(NOW.time))
    }

    return accountingDetailList
  }

  private fun createHeader(createTime: Date): MainAccountingDetailHeader {
    val title: String = createHeaderTitle(createTime)
    val sum = accountingDao.sumOfDay(oneDayFormat.format(createTime))
    val sumString: String = applicationContext.getString(
        R.string.main_accounting_detail_header_sum, sum)
    return MainAccountingDetailHeader(title, sumString)
  }

  private fun createDetailContent(accounting: Accounting): MainAccountingDetailContent {
    return MainAccountingDetailContent(
        accounting.id,
        applicationContext.getString(R.string.amount_format, accounting.amount),
        accounting.tagName,
        accounting.remarks,
        timeFormat.format(accounting.createTime),
        accounting.createTime)
  }

  private fun createHeaderTitle(createTime: Date): String = oneDayFormat.format(createTime)

  private fun findAndUpdateHeader(
      list: MutableList<MainAccountingDetail>,
      addOrUpdateIndex: Int) {
    list.indexOfLast { it is MainAccountingDetailHeader && list.indexOf(it) < addOrUpdateIndex }
        .apply {
          val createTime = (list[addOrUpdateIndex] as MainAccountingDetailContent).createTime
          val sum = accountingDao.sumOfDay(oneDayFormat.format(createTime))
          val sumString: String = applicationContext.getString(
              R.string.main_accounting_detail_header_sum, sum)
          list[this] = (list[this] as MainAccountingDetailHeader).copy(total = sumString)
        }
  }

}