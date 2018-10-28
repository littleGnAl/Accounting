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

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.littlegnal.accounting.base.DB_NAME
import com.littlegnal.accounting.base.eventbus.RxBus
import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.base.schedulers.SchedulerProvider
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.AppDataBase
import com.littlegnal.accounting.ui.addedit.AddOrEditBuilderModule
import com.littlegnal.accounting.ui.main.MainBuilderModule
import com.littlegnal.accounting.ui.summary.SummaryBuilderModule
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton

@Module(
    includes = [
      ViewModelModule::class,
      AddOrEditBuilderModule::class,
      SummaryBuilderModule::class,
      MainBuilderModule::class
    ]
)
class AppModule {

  @Singleton
  @Provides
  fun provideAppDataBase(application: Application): AppDataBase {
    return Room.databaseBuilder(application, AppDataBase::class.java, DB_NAME)
        .build()
  }

  @Singleton
  @Provides
  fun provideApplicationContext(application: Application): Context = application.applicationContext

  @Singleton
  @Provides
  fun provideAccountingDao(appDataBase: AppDataBase): AccountingDao = appDataBase.accountingDao()

  @Singleton
  @Provides
  fun provideAddOrUpdateAccountingObservable(): PublishSubject<Accounting> = PublishSubject.create()

  @Singleton
  @Provides
  fun provideBaseSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider()

  @Singleton
  @Provides
  fun provideRxBus() = RxBus()
}
