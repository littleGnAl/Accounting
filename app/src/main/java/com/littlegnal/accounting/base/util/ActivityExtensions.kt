package com.yunmai.scale.coach.common.extensions

import android.app.Activity
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

/**
 * @author littlegnal
 * @date 2017/12/14
 */

/**
 * [Toast]扩展方法
 */
fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, text, duration).show()
}