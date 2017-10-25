package com.littlegnal.accounting

import android.app.Activity
import android.app.Application
import com.littlegnal.accounting.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/8/7
 */
open class App : Application(), HasActivityInjector {

  @Inject
  lateinit var dispatchActivityInjector: DispatchingAndroidInjector<Activity>

  override fun onCreate() {
    super.onCreate()

    AppInjector.initDI(this)
  }

  override fun activityInjector(): AndroidInjector<Activity> = dispatchActivityInjector
}