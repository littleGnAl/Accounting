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

package com.littlegnal.accounting.ui.main.adapter

import androidx.appcompat.widget.AppCompatTextView
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

@EpoxyModelClass(layout = R.layout.main_accounting_detail_header_layout)
abstract class MainAccountingDetailHeaderModel :
    EpoxyModelWithHolder<MainAccountingDetailHeaderModel.HeaderHolder>() {

  @EpoxyAttribute var title: String? = null
  @EpoxyAttribute var total: String? = null

  override fun bind(holder: HeaderHolder) {
    super.bind(holder)

    holder?.header?.text = title
    holder?.total?.text = total
  }

  class HeaderHolder : EpoxyHolder() {

    lateinit var header: AppCompatTextView
    lateinit var total: AppCompatTextView

    override fun bindView(itemView: View) {
      itemView?.let {
        header = itemView.findViewById(R.id.main_accounting_detail_header_title)
        total = itemView.findViewById(R.id.main_accounting_detail_header_total)
      }
    }
  }
}
