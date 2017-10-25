package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.ui.MviViewTestImpl
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * @author littlegnal
 * @date 2017/10/24
 */
class AddOrEditViewTestImpl(
    addOrEditPresenter: AddOrEditPresenter) : MviViewTestImpl<AddOrEditViewState>() {

  private val loadDataPublisher = PublishSubject.create<Int>()
  private val saveOrUpdatePublisher = PublishSubject.create<Array<String>>()

  init {
    addOrEditPresenter.attachView(object : AddOrEditView {
      override fun render(viewState: AddOrEditViewState) {
        renderEvents.add(viewState)
        renderEventPublisher.onNext(viewState)
      }

      override fun loadDataIntent(): Observable<Int> = loadDataPublisher

      override fun saveOrUpdateIntent(): Observable<Array<String>> = saveOrUpdatePublisher

    })
  }

  fun fireLoadDataIntent(id: Int) = loadDataPublisher.onNext(id)

  fun fireSaveOrUpdateIntent(values: Array<String>) = saveOrUpdatePublisher.onNext(values)
}