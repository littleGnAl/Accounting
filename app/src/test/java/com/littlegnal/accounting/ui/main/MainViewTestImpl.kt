package com.littlegnal.accounting.ui.main

import com.littlegnal.accounting.ui.MviViewTestImpl
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * copy from [mosby](https://github.com/sockeqwe/mosby/blob/master/sample-mvi/src/test/java/com/hannesdorfmann/mosby3/sample/mvi/view/home/HomeViewRobot.java)
 *
 * @author littlegnal
 * @date 2017/10/22
 */
class MainViewTestImpl(mainPresenter: MainPresenter): MviViewTestImpl<MainViewState>() {

  private val loadFirstPagePublisher = PublishSubject.create<Boolean>()
  private val loadNextPagePublisher = PublishSubject.create<Date>()
  private val deleteAccountingPublisher = PublishSubject.create<Int>()

  init {
    mainPresenter.attachView(object : MainView {
      override fun render(viewState: MainViewState) {
        renderEvents.add(viewState)
        renderEventPublisher.onNext(viewState)
      }

      override fun loadFirstPageIntent(): Observable<Boolean> {
        return loadFirstPagePublisher
      }

      override fun loadNextPageIntent(): Observable<Date> {
        return loadNextPagePublisher
      }

      override fun deleteAccountingIntent(): Observable<Int> {
        return deleteAccountingPublisher
      }
    })
  }

  fun fireLoadFirstPageIntent() = loadFirstPagePublisher.onNext(true)

  fun fireLoadNextPageIntent(lastDate: Date) = loadNextPagePublisher.onNext(lastDate)

  fun fireDeleteAccountingIntent(id: Int) = deleteAccountingPublisher.onNext(id)

}