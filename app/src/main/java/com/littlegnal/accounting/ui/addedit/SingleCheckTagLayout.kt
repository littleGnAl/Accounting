package com.littlegnal.accounting.ui.addedit

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatCheckedTextView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.util.dip
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * @author littlegnal
 * @date 2017/8/27
 */
class SingleCheckTagLayout : FlexboxLayout {

  private var preCheckedTag: AppCompatCheckedTextView? = null

  private val tagNamePublisher: PublishSubject<String> = PublishSubject.create()

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: kotlin.Int) :
      super(context, attrs, defStyleAttr)

  override fun onFinishInflate() {
    super.onFinishInflate()

    initTags()
    flexWrap = FlexWrap.WRAP
  }

  private fun initTags() {
    val tags = context.resources.getStringArray(R.array.accounting_tag_list)
    for (tag: String in tags) {
      val tagView = AppCompatCheckedTextView(context)
      tagView.text = tag
      tagView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f)
      tagView.setBackgroundResource(R.drawable.add_or_edit_tag_bg)
      tagView.setPadding(dip(10), 0, dip(10), 0)
      tagView.setTextColor(AppCompatResources.getColorStateList(
          context, R.color.add_or_edit_tag_text_color))
      tagView.gravity = Gravity.CENTER
      val tagViewLp = FlexboxLayout.LayoutParams(
          FlexboxLayout.LayoutParams.WRAP_CONTENT,
          dip(36))
      tagViewLp.bottomMargin = dip(6)
      tagViewLp.rightMargin = dip(6)

      tagView.setOnClickListener {
        preCheckedTag?.toggle()
        (it as AppCompatCheckedTextView).toggle()
        tagNamePublisher.onNext(it.text.toString())
        preCheckedTag = it
      }

      addView(tagView, tagViewLp)
    }
  }

  fun getCheckedTagName(): Observable<String> = tagNamePublisher

  fun selectTag(tagName: String) {
    (0 until childCount)
        .map { getChildAt(it) }
        .filter { it is AppCompatCheckedTextView }
        .map { it as AppCompatCheckedTextView }
        .filter { tagName == it.text }
        .forEach {
          it.isChecked = true
          tagNamePublisher.onNext(it.text.toString())
        }
  }
}