package com.littlegnal.accounting.base.util

import android.content.Context
import android.os.Build

/**
 * @author littlegnal
 * @date 2017/10/4
 */
fun Context.colorRes(colorResId: Int): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resources.getColor(colorResId, theme)
    } else {
      resources.getColor(colorResId)
    }
