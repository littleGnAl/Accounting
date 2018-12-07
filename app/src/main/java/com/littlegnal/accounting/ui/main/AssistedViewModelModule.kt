package com.littlegnal.accounting.ui.main

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_AssistedViewModelModule::class])
abstract class AssistedViewModelModule
