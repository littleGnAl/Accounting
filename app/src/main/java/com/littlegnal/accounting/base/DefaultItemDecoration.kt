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

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyModel
import com.littlegnal.accounting.base.util.dip

/**
 * 项目中默认的[RecyclerView.ItemDecoration]，使用[isDrawableDividerItem]方法来控制是否绘制分割线
 */
class DefaultItemDecoration(
    private val epoxyControllerAdapter: EpoxyControllerAdapter,
    private val isDrawableDividerItem: (EpoxyModel<*>) -> Boolean
) : RecyclerView.ItemDecoration() {
  
  private val divider: ColorDrawable = ColorDrawable(0xfff3f3f3.toInt())

  override fun onDrawOver(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
    val childCount: Int? = parent?.childCount
    for (i in 0 until childCount!!) {
      val child: View = parent.getChildAt(i)
      val adapterPosition: Int = parent.getChildAdapterPosition(child)
      if (adapterPosition >= 0 && adapterPosition < parent.adapter.itemCount - 1) {
        val epoxyModel: EpoxyModel<*> = epoxyControllerAdapter.getModelAtPosition(adapterPosition)
        if (isDrawableDividerItem(epoxyModel)) {
          c?.save()
          divider.setBounds(
              parent.dip(16),
              child.bottom,
              parent.width,
              child.bottom + parent.dip(1))
          divider.draw(c)
          c?.restore()
        }
      }
    }
  }
}