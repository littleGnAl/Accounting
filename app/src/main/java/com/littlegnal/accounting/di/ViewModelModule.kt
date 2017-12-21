package com.littlegnal.accounting.di

import android.arch.lifecycle.ViewModelProvider
import com.littlegnal.accounting.base.ViewModelFactory
import dagger.Binds
import dagger.Module

/**
 * @author littlegnal
 * @date 2017/12/11
 */
@Module
abstract class ViewModelModule {

  @Binds
  abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}