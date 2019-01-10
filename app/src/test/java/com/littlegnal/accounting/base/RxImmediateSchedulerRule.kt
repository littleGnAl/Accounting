package com.littlegnal.accounting.base

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.exceptions.CompositeException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RxImmediateSchedulerRule : TestRule {
  private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun apply(
      base: Statement,
      description: Description
    ): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
              RxAndroidPlugins.reset()
              RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
              RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
              setRxImmediateSchedulers()
              try {
                base.evaluate()
              } finally {
                RxAndroidPlugins.reset()
                clearRxImmediateScheduleres()
              }
            }
        }
    }

  private fun setRxImmediateSchedulers() {
    RxJavaPlugins.reset()
    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setSingleSchedulerHandler { Schedulers.trampoline() }
    // This is necessary to prevent rxjava from swallowing errors
    // https://github.com/ReactiveX/RxJava/issues/5234
    defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
      if (e is CompositeException && e.exceptions.size == 1) throw e.exceptions[0]
      throw e
    }
  }

  private fun clearRxImmediateScheduleres() {
    Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler)
    defaultExceptionHandler = null
    RxJavaPlugins.reset()
  }
}
