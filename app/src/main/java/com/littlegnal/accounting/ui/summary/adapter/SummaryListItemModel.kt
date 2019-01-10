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

package com.littlegnal.accounting.ui.summary.adapter

import androidx.appcompat.widget.AppCompatTextView
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

@EpoxyModelClass(layout = R.layout.summary_list_item_layout)
abstract class SummaryListItemModel :
    EpoxyModelWithHolder<SummaryListItemModel.SummaryListItemHolder>() {

  @EpoxyAttribute var tag: String? = null
  @EpoxyAttribute var total: String? = null

  override fun bind(holder: SummaryListItemHolder) {
    holder.tag?.text = tag
    holder.total?.text = total
  }

  class SummaryListItemHolder : EpoxyHolder() {

    var tag: AppCompatTextView? = null
    var total: AppCompatTextView? = null

    override fun bindView(itemView: View) {
      tag = itemView.findViewById(R.id.tv_summary_list_item_tag)
      total = itemView.findViewById(R.id.tv_summary_list_item_total)
    }
  }
}
