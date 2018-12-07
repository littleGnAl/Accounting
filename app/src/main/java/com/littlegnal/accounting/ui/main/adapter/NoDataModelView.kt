package com.littlegnal.accounting.ui.main.adapter

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT
import com.littlegnal.accounting.R

@ModelView(autoLayout = MATCH_WIDTH_MATCH_HEIGHT)
class NoDataModelView : AppCompatTextView {

  constructor(context: Context?) : super(context)

  constructor(
    context: Context?,
    attrs: AttributeSet?
  ) : super(context, attrs)

  constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr)

  init {
    setText(R.string.main_accounting_no_accounting_records)
    setTextColor(0xff888888.toInt())
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f)
    gravity = Gravity.CENTER
  }
}
