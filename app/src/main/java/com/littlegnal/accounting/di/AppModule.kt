package com.littlegnal.accounting.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.littlegnal.accounting.base.DB_NAME
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.db.AppDataBase
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject

/**
 * @author littlegnal
 * @date 2017/8/8
 */
@Module
class AppModule {

  @AppRoot
  @Provides
  fun provideAppDataBase(application: Application): AppDataBase {
    return Room.databaseBuilder(application, AppDataBase::class.java, DB_NAME)
        .build()
  }

  @AppRoot
  @Provides
  fun provideApplicationContext(application: Application): Context = application.applicationContext

  @AppRoot
  @Provides
  fun provideAccountingDao(appDataBase: AppDataBase): AccountingDao = appDataBase.accountingDao()

  @AppRoot
  @Provides
  fun provideAddOrUpdateAccountingObservable(): PublishSubject<Accounting> = PublishSubject.create()
}