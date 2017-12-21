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
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton

/**
 * @author littlegnal
 * @date 2017/8/8
 */
@Module(includes = [ViewModelModule::class, AddOrEditBuilderModule::class])
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