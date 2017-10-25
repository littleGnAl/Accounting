package com.littlegnal.accounting.ui.main.adapter

import com.airbnb.epoxy.Typed2EpoxyController
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/23
 */
class MainAccountingDetailController :
    Typed2EpoxyController<List<MainAccountingDetail>, Boolean>() {

  var isNoMoreData: Boolean = false
  var isLoadingMore: Boolean = false
  var accountingDetailList: List<MainAccountingDetail>? = null
  var lastDate: Date? = null
  private val itemClickPublisher: PublishSubject<Int> = PublishSubject.create()
  private val itemLongClickPublisher: PublishSubject<Int> = PublishSubject.create()

  override fun buildModels(
      list: List<MainAccountingDetail>?,
      isLoading: Boolean) {
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
              val clickContent = accountingDetailList?.get(position) as MainAccountingDetailContent
              itemClickPublisher.onNext(clickContent.id)
            }
            longClickListener { _, _, _, position ->
              val clickContent = accountingDetailList?.get(position) as MainAccountingDetailContent
              itemLongClickPublisher.onNext(clickContent.id)
              return@longClickListener true
            }
          }
        }
      }
    }

    if (isLoading) mainAccountingDetailLoading { id("loading") }
  }

  override fun setData(data1: List<MainAccountingDetail>?, data2: Boolean) {
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