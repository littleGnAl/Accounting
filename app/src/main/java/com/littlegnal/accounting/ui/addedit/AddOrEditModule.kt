package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.schedulers.BaseSchedulerProvider
import com.littlegnal.accounting.db.AccountingDao
import dagger.Module
import dagger.Provides

/**
 * @author littlegnal
 * @date 2017/12/19
 */
@Module
class AddOrEditModule {

  @Provides
  fun provideAddOrEditActionProcessorHolder(
      schedulerProvider: BaseSchedulerProvider,
      accountingDao: AccountingDao) =
      AddOrEditActionProcessorHolder(schedulerProvider, accountingDao)

}