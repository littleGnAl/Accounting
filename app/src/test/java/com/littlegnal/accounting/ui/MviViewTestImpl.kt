package com.littlegnal.accounting.ui

import com.littlegnal.accounting.base.mvi.MviViewState
import io.reactivex.subjects.ReplaySubject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import java.util.concurrent.TimeUnit

/**
 * @author littlegnal
 * @date 2017/10/24
 */
open class MviViewTestImpl<VS: MviViewState> {
  protected val renderEvents = mutableListOf<VS>()
  protected val renderEventPublisher = ReplaySubject.create<VS>()

  open fun assertViewStateRendered(vararg expectedViewStates: VS) {
    val eventCount = expectedViewStates.size
    renderEventPublisher.take(eventCount.toLong())
        .timeout(10, TimeUnit.SECONDS)
        .blockingSubscribe()

    if (renderEventPublisher.values.size > eventCount) {
      Assert.fail("Expected to wait for $eventCount , but there were " +
          "${renderEventPublisher.values.size} Events in total, which is more than expected: " +
          renderEventPublisher.values.joinToString())
    }

    assertThat(expectedViewStates.toMutableList(), Matchers.`is`((renderEvents)))
  }
}