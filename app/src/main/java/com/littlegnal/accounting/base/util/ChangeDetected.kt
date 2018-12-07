package com.littlegnal.accounting.base.util

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

class ChangeDetected<T>(initialValue: T, private val onChange: (T) -> Unit)
    : ObservableProperty<T>(initialValue) {

    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
        return oldValue != newValue
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        onChange(newValue)
    }
}
