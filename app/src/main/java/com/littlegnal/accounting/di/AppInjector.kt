package com.littlegnal.accounting.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.littlegnal.accounting.App
import dagger.android.AndroidInjection

/**
 * @author littlegnal
 * @date 2017/8/8
 */
class AppInjector {
  companion object {
    fun initDI(app: App) {
      DaggerAppComponent.builder().application(app).build().inject(app)
      app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity?) {

        }

        override fun onActivityResumed(p0: Activity?) {
        }

        override fun onActivityStarted(p0: Activity?) {
        }

        override fun onActivityDestroyed(p0: Activity?) {
        }

        override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
        }

        override fun onActivityStopped(p0: Activity?) {
        }

        override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {
          AndroidInjection.inject(activity)
        }
      })
    }
  }
}