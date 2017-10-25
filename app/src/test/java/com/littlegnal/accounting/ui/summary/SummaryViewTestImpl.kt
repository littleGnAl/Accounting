package com.littlegnal.accounting.ui.summary

import com.littlegnal.accounting.ui.MviViewTestImpl
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * @author littlegnal
 * @date 2017/10/24
 */
class SummaryViewTestImpl(
    summaryPresenter: SummaryPresenter) : MviViewTestImpl<SummaryViewState>() {

  private val loadDataPublisher = PublishSubject.create<Boolean>()

  private val monthClickedPublisher = PublishSubject.create<Date>()

  init {
    summaryPresenter.attachView(object : SummaryView {
      override fun render(viewState: SummaryViewState) {
        renderEvents.add(viewState)
        renderEventPublisher.onNext(viewState)
      }

      override fun loadDataIntent(): Observable<Boolean> = loadDataPublisher

      override fun monthClickedIntent(): Observable<Date> = monthClickedPublisher
    })
  }

  fun fireLoadDataIntent() = loadDataPublisher.onNext(true)

  fun fireMonthClickedIntent(date: Date) = monthClickedPublisher.onNext(date)
}