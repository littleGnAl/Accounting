package com.littlegnal.accounting.base

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.littlegnal.accounting.R

/**
 * @author littlegnal
 * @date 2017/12/16
 */
open class BaseActivity : AppCompatActivity() {
  protected lateinit var toolbar: Toolbar

  override fun setContentView(layoutResID: Int) {
    super.setContentView(layoutResID)
    initToolbar()
  }

  override fun setContentView(view: View?) {
    super.setContentView(view)
    initToolbar()
  }

  override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
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
          Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      if (currentFocus != null) {
        inputMethodManager?.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS)
      }
    }
  }
}