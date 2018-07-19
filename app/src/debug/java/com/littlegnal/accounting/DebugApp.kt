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

import android.app.Application
import com.facebook.soloader.SoLoader
import com.facebook.sonar.android.AndroidSonarClient
import com.facebook.sonar.android.utils.SonarUtils
import com.facebook.sonar.plugins.inspector.DescriptorMapping
import com.facebook.sonar.plugins.inspector.InspectorSonarPlugin
import com.facebook.sonar.plugins.sharedpreferences.SharedPreferencesSonarPlugin
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

/**
 * Debug variant [Application]，用于配置Debug模式下的工具，如*LeakCanary*，*Stetho*等
 */
class DebugApp : App() {

  override fun onCreate() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }
    LeakCanary.install(this)

    super.onCreate()
    Stetho.initializeWithDefaults(this)

    Timber.plant(Timber.DebugTree())

    SoLoader.init(this, false)

    if (SonarUtils.shouldEnableSonar(this)) {
      with(AndroidSonarClient.getInstance(this)) {
        addPlugin(InspectorSonarPlugin(this@DebugApp, DescriptorMapping.withDefaults()))
        addPlugin(SharedPreferencesSonarPlugin(this@DebugApp))

        start()
      }
    }
  }
}