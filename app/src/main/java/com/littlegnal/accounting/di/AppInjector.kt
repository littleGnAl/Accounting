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

package com.littlegnal.accounting.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.littlegnal.accounting.App
import dagger.android.AndroidInjection

class AppInjector {
  companion object {
    fun initDI(app: App) {
      DaggerAppComponent.builder()
          .application(app)
          .build()
          .inject(app)
      app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity?) {
        }

        override fun onActivityResumed(p0: Activity?) {
        }

        override fun onActivityStarted(p0: Activity?) {
        }

        override fun onActivityDestroyed(p0: Activity?) {
        }

        override fun onActivitySaveInstanceState(
          p0: Activity?,
          p1: Bundle?
        ) {
        }

        override fun onActivityStopped(p0: Activity?) {
        }

        override fun onActivityCreated(
          activity: Activity?,
          bundle: Bundle?
        ) {
          AndroidInjection.inject(activity)
        }
      })
    }
  }
}