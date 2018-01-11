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

import android.annotation.SuppressLint
import android.content.Context
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.mvi.*
import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val ONE_PAGE_SIZE = 15

/**
 * 用于处理所有[MviAction]的业务逻辑，并把[MviAction]转换成[MviResult]
 *
 * 这里为了让Reducer在[BaseSchedulerProvider.io]线程中执行，在processor合并之后就进行[Observable.scan]
 * 处理
 * @see actionProcessorWithReducer
 */
class MainActionProcessorHolder(
    private val schedulerProvider: BaseSchedulerProvider,
    private val applicationContext: Context,
    private val accountingDao: AccountingDao) {

  @SuppressLint("SimpleDateFormat")
  private val dateNumFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
  @SuppressLint("SimpleDateFormat")
  private val oneDayFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
  @SuppressLint("SimpleDateFormat")
  private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")

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

  private fun createHeader(createTime: Date): MainAccountingDetailHeader {
    val title: String = createHeaderTitle(createTime)
    val sum = accountingDao.sumOfDay(oneDayFormat.format(createTime))
    val sumString: String = applicationContext.getString(
        R.string.main_accounting_detail_header_sum, sum)
    return MainAccountingDetailHeader(title, sumString)
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

  private val loadAccountingsProcessor =
      ObservableTransformer<MainAction.LoadAccountingsAction, MainResult.LoadAccountingsResult> {
        actions -> actions.flatMap { action ->
          accountingDao.queryPreviousAccounting(action.lastDate, ONE_PAGE_SIZE.toLong())
              .toObservable()
              .map { MainResult.LoadAccountingsResult.success(action.lastDate, it) }
              .onErrorReturn {
                Timber.e(it)
                MainResult.LoadAccountingsResult.failure(it)
              }
              .subscribeOn(schedulerProvider.io())
              .startWith(MainResult.LoadAccountingsResult.inFlight())
        }
      }

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

  private val deleteAccountingProcessor =
      ObservableTransformer<MainAction.DeleteAccountingAction, MainResult.DeleteAccountingResult> {
        actions -> actions.flatMap { action ->
          Observable.fromCallable {
            accountingDao.deleteAccountingById(action.deletedId)
            MainResult.DeleteAccountingResult.success(action.deletedId)
          }
          .onErrorReturn {
            Timber.e(it)
            MainResult.DeleteAccountingResult.failure(it)
          }
          .subscribeOn(schedulerProvider.io())
          .startWith(MainResult.DeleteAccountingResult.inFlight())
        }
      }

  private val addAccountingProcessor =
      ObservableTransformer<MainAction.AddAccountingAction, MainResult.AddAccountingResult> {
        actions -> actions.flatMap {
          Observable.fromCallable {
            MainResult.AddAccountingResult.success(it.accounting)
          }
          .subscribeOn(schedulerProvider.io())
        }
  }

  private val updateAccountingProcessor =
      ObservableTransformer<MainAction.UpdateAccountingAction, MainResult.UpdateAccountingResult> {
        actions -> actions.flatMap {
          Observable.fromCallable {
            MainResult.UpdateAccountingResult.success(it.accounting)
          }
          .subscribeOn(schedulerProvider.io())
        }
      }

  /**
   * /**
   * 拆分[Observable<MviAction>]并且为不同的[MviAction]提供相应的processor，processor用于处理业务逻辑，
   * 同时把[MviAction]转换为[MviResult]，最终通过[Observable.merge]合并回一个流
   *
   * 这里为了让Reducer在[BaseSchedulerProvider.io]线程中执行，在processor合并之后就进行[Observable.scan]
   * 处理
   *
   * 为了防止遗漏[MviAction]未处理，在流的最后合并一个错误检测，方便维护
  */
   */
  val actionProcessorWithReducer = ObservableTransformer<MainAction, MainViewState> {
    actions -> actions.publish {
      shared -> Observable.merge(listOf(
          shared.ofType(MainAction.LoadAccountingsAction::class.java)
              .compose(loadAccountingsProcessor),
          shared.ofType(MainAction.DeleteAccountingAction::class.java)
              .compose(deleteAccountingProcessor),
          shared.ofType(MainAction.AddAccountingAction::class.java).compose(addAccountingProcessor),
          shared.ofType(MainAction.UpdateAccountingAction::class.java)
              .compose(updateAccountingProcessor)
        ))
        .mergeWith(shared.filter {
              it !is MainAction.LoadAccountingsAction &&
                  it !is MainAction.DeleteAccountingAction &&
                  it !is MainAction.AddAccountingAction &&
                  it !is MainAction.UpdateAccountingAction
            }
            .flatMap {
              Observable.error<MainResult>(
                  IllegalArgumentException("Unknown Action type: $it"))
            })
        .scan(MainViewState.idle(), reducer).observeOn(schedulerProvider.ui())
    }
  }

  private fun createLoadAccountingsViewState(
      previousState: MainViewState,
      result: MainResult.LoadAccountingsResult): MainViewState {
    val accountingList = result.accountingList
    val lastDate = result.lastDate
    val newAdapterList = previousState.accountingDetailList.toMutableList()

    val isFirstPage = newAdapterList.isEmpty()
    return if (isFirstPage) {
      newAdapterList.addAll(createFirstPageList(lastDate, accountingList))
      previousState.copy(
          error = null,
          isLoading = false,
          accountingDetailList = newAdapterList,
          isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
          isNoData = accountingList.isEmpty()
      )
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
      }.let {
        previousState.copy(
            error = null,
            isLoading = false,
            accountingDetailList = newAdapterList,
            isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
            isNoData = false)
      }
    }
  }

  private fun createDeleteAccountingViewState(
      previousState: MainViewState,
      result: MainResult.DeleteAccountingResult): MainViewState {
    val newAccountingList = previousState.accountingDetailList.toMutableList()

    val deleteContentIndex: Int = newAccountingList.indexOfLast {
      it is MainAccountingDetailContent && it.id == result.deletedId
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

    return previousState.copy(
        isLoading = false,
        error = null,
        accountingDetailList = newAccountingList)
  }

  private fun createAddAccountingViewState(
      previousState: MainViewState,
      result: MainResult.AddAccountingResult): MainViewState {
    val addOrEditAccounting = result.accounting
    val newContent = createDetailContent(addOrEditAccounting)
    val newAccountingList = previousState.accountingDetailList.toMutableList()
    if (newAccountingList.isEmpty()) {
      newAccountingList.add(createHeader(addOrEditAccounting.createTime))
      newAccountingList.add(newContent)
    } else {
      val title: String = createHeaderTitle(addOrEditAccounting.createTime)
      val header: MainAccountingDetailHeader =
          newAccountingList[0] as MainAccountingDetailHeader

      newAccountingList.indexOfFirst {
        it is MainAccountingDetailContent &&
            it.createTime <= addOrEditAccounting.createTime
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

    return previousState.copy(
        isLoading = false,
        error = null,
        accountingDetailList = newAccountingList,
        isNoData = false)
  }

  private fun createUpdateAccountingViewState(
      previousState: MainViewState,
      result: MainResult.UpdateAccountingResult): MainViewState {
    val addOrEditAccounting = result.accounting
    val newContent = createDetailContent(addOrEditAccounting)
    val newAccountingList = previousState.accountingDetailList.toMutableList()
    val oldContent = newAccountingList
        .filter { it is MainAccountingDetailContent }
        .map { it as MainAccountingDetailContent }
        .find { it.id == newContent.id }

    oldContent?.apply {
      newAccountingList.indexOf(oldContent).apply {
        findAndUpdateHeader(newAccountingList, this)
        newAccountingList[this] = newContent
      }
    }

    return previousState.copy(
        isLoading = false,
        error = null,
        accountingDetailList = newAccountingList)
  }

  /**
   * 使用最后一次缓存的[MviViewState]和最新的[MviResult]来创建新的[MviViewState]，通过[MviView.render]方法
   * 把新的[MviViewState]渲染到界面
   */
  private val reducer = BiFunction<MainViewState, MainResult, MainViewState> {
        previousState, result ->
        when(result) {
          is MainResult.LoadAccountingsResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> { createLoadAccountingsViewState(previousState, result) }
              LceStatus.FAILURE -> { previousState.copy(error = result.error, isLoading = false) }
              LceStatus.IN_FLIGHT -> { previousState.copy(error = null, isLoading = true) }
            }
          }
          is MainResult.DeleteAccountingResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> { createDeleteAccountingViewState(previousState, result) }
              LceStatus.FAILURE -> { previousState.copy(isLoading = false, error = result.error) }
              LceStatus.IN_FLIGHT -> { previousState.copy(isLoading = true, error = null) }
            }
          }
          is MainResult.AddAccountingResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> { createAddAccountingViewState(previousState, result) }
              LceStatus.FAILURE -> { previousState.copy(isLoading = false, error = result.error) }
              LceStatus.IN_FLIGHT -> { previousState.copy(isLoading = true, error = null) }
            }
          }
          is MainResult.UpdateAccountingResult -> {
            when(result.status) {
              LceStatus.SUCCESS -> { createUpdateAccountingViewState(previousState, result) }
              LceStatus.FAILURE -> { previousState.copy(isLoading = false, error = result.error) }
              LceStatus.IN_FLIGHT -> { previousState.copy(isLoading = true, error = null) }
            }
          }
        }
      }
}