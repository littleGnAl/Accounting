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

package com.littlegnal.accounting.ui.main.adapter

import com.airbnb.epoxy.Typed2EpoxyController
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MainAccountingDetailController :
    Typed2EpoxyController<List<MainAccountingDetail>, Boolean>() {

  var isNoMoreData: Boolean = false
  var isLoadingMore: Boolean = false
  lateinit var accountingDetailList: List<MainAccountingDetail>

  private val itemClickPublisher: PublishSubject<Int> = PublishSubject.create()
  private val itemLongClickPublisher: PublishSubject<Int> = PublishSubject.create()

  override fun buildModels(
    list: List<MainAccountingDetail>?,
    isLoading: Boolean
  ) {
    list?.apply {
      for (accountingDetail in this) {
        if (accountingDetail is MainAccountingDetailHeader) {
          mainAccountingDetailHeader {
            id(accountingDetail.title)
            title(accountingDetail.title)
            total(accountingDetail.total)
          }
        }

        if (accountingDetail is MainAccountingDetailContent) {
          mainAccountingDetailContent {
            id(accountingDetail.id)
            time(accountingDetail.time)
            tagName(accountingDetail.tagName)
            remarks(accountingDetail.remarks)
            amount(accountingDetail.amount)
            clickListener { _, _, _, position ->
              val clickContent = accountingDetailList[position] as MainAccountingDetailContent
              itemClickPublisher.onNext(clickContent.id)
            }
            longClickListener { _, _, _, position ->
              val clickContent = accountingDetailList[position] as MainAccountingDetailContent
              itemLongClickPublisher.onNext(clickContent.id)
              return@longClickListener true
            }
          }
        }
      }
    }

    if (isLoading) mainAccountingDetailLoading { id("loading") }
  }

  override fun setData(
    data1: List<MainAccountingDetail>,
    data2: Boolean
  ) {
    accountingDetailList = data1
    isLoadingMore = data2
    super.setData(data1, data2)
  }

  fun getItemClickObservable(): Observable<Int> {
    return itemClickPublisher
  }

  fun getItemLongClickObservable(): Observable<Int> {
    return itemLongClickPublisher
  }
}
