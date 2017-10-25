package com.littlegnal.accounting.di

import com.littlegnal.accounting.ui.addedit.AddOrEditActivity
import com.littlegnal.accounting.ui.main.MainActivity
import com.littlegnal.accounting.ui.summary.SummaryActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * @author littlegnal
 * @date 2017/8/8
 */
@Module
abstract class ActivityModule {

  @ActivityScope
  @ContributesAndroidInjector(modules = arrayOf(MainActivityModule::class))
  abstract fun contributeMainActivity(): MainActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = arrayOf(AddOrEditActivityModule::class))
  abstract fun contributeAddOrEditActivity(): AddOrEditActivity

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun contributeSummaryActivity(): SummaryActivity
}