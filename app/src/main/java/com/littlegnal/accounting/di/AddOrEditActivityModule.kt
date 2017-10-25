package com.littlegnal.accounting.di

import com.littlegnal.accounting.ui.addedit.AddOrEditActivity
import com.littlegnal.accounting.ui.addedit.AddOrEditNavigator
import dagger.Binds
import dagger.Module

/**
 * @author littlegnal
 * @date 2017/8/24
 */
@Module
abstract class AddOrEditActivityModule {

  @Binds
  abstract fun provideAddOrEditActivity(addOrEditActivity: AddOrEditActivity): AddOrEditNavigator
}