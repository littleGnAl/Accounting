package com.littlegnal.accounting.base.eventbus

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable



/**
 * @author littlegnal
 * @date 2017/12/18
 */
class RxBus {
  private val bus = PublishRelay.create<Any>().toSerialized()

  fun send(o: Any) {
    bus.accept(o)
  }

  fun asFlowable(): Flowable<Any> {
    return bus.toFlowable(BackpressureStrategy.LATEST)
  }

  fun hasObservers(): Boolean {
    return bus.hasObservers()
  }
}