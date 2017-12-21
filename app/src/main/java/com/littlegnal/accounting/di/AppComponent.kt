package com.littlegnal.accounting.di

import android.app.Application
import com.littlegnal.accounting.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * @author littlegnal
 * @date 2017/8/8
 */
@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    ActivityModule::class])
interface AppComponent {

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(app: App)
}