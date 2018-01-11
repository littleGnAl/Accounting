/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting

import android.app.Activity
import android.app.Application
import com.littlegnal.accounting.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * 自定义[Application]
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