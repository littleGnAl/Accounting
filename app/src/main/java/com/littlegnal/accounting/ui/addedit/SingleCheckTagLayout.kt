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

package com.littlegnal.accounting.ui.addedit

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatCheckedTextView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.jakewharton.rxrelay2.PublishRelay
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.util.dip
import io.reactivex.Observable

/**
 * 标签列表[FlexboxLayout]
 */
class SingleCheckTagLayout : FlexboxLayout {

  private var preCheckedTag: AppCompatCheckedTextView? = null

  private val tagNamePublisher: PublishRelay<String> = PublishRelay.create()

  private lateinit var checkedTagName: String

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
        checkedTagName = it.text.toString()
        tagNamePublisher.accept(checkedTagName)
        preCheckedTag = it
      }

      addView(tagView, tagViewLp)
    }
  }

  /**
   * 已选择标签[Observable]
   */
  fun checkedTagNameObservable(): Observable<String> = tagNamePublisher

  /**
   * 获取已选择的标签名
   *
   * @return 已选择的标签名
   */
  fun getCheckedTagName(): String = checkedTagName

  /**
   * 根据标签名称，设置标签为选中状态
   *
   * @param tagName 标签名称
   */
  fun selectTag(tagName: String) {
    (0 until childCount)
        .map { getChildAt(it) }
        .filter { it is AppCompatCheckedTextView }
        .map { it as AppCompatCheckedTextView }
        .filter { tagName == it.text }
        .forEach {
          it.isChecked = true
          checkedTagName = it.text.toString()
          tagNamePublisher.accept(checkedTagName)
        }
  }
}