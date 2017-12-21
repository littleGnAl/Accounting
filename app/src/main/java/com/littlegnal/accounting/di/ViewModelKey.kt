package com.littlegnal.accounting.di

import android.arch.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * @author littlegnal
 * @date 2017/12/11
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)