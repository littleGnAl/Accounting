package com.littlegnal.accounting

import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

/**
 * @author littlegnal
 * @date 2017/8/7
 */
class DebugApp : App() {

  override fun onCreate() {
    super.onCreate()

    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }
    LeakCanary.install(this)

    Stetho.initializeWithDefaults(this);

    Timber.plant(Timber.DebugTree())
  }
}