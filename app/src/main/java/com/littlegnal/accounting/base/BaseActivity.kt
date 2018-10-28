/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting.base

import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.airbnb.mvrx.BaseMvRxActivity
import com.littlegnal.accounting.R

open class BaseActivity : BaseMvRxActivity() {
  protected lateinit var toolbar: Toolbar

  override fun setContentView(layoutResID: Int) {
    super.setContentView(layoutResID)
    initToolbar()
  }

  override fun setContentView(view: View?) {
    super.setContentView(view)
    initToolbar()
  }

  override fun setContentView(
    view: View?,
    params: ViewGroup.LayoutParams?
  ) {
    super.setContentView(view, params)
    initToolbar()
  }

  private fun initToolbar() {
    toolbar = findViewById(R.id.base_toolbar)
    setSupportActionBar(toolbar)
  }

  fun setToolbarWithBack() {
    toolbar.setNavigationOnClickListener { onBackPressed() }
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
    actionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private var inputMethodManager: InputMethodManager? = null

  fun hideSoftKeyboard() {
    if (inputMethodManager == null) {
      inputMethodManager = getSystemService(
          Context.INPUT_METHOD_SERVICE
      ) as InputMethodManager
    }
    if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      if (currentFocus != null) {
        inputMethodManager?.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
      }
    }
  }
}
