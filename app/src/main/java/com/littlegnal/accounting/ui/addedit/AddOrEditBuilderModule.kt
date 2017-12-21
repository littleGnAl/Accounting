package com.littlegnal.accounting.ui.addedit

import android.arch.lifecycle.ViewModel
import com.littlegnal.accounting.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * @author littlegnal
 * @date 2017/12/19
 */

@Module(includes = [AddOrEditModule::class])
abstract class AddOrEditBuilderModule {

  @ContributesAndroidInjector
  abstract fun contributeAddOrEditActivity(): AddOrEditActivity

  @Binds
  @IntoMap
  @ViewModelKey(AddOrEditViewModel::class)
  abstract fun bindAddOrEditModel(addOrEditViewModel: AddOrEditViewModel): ViewModel
}