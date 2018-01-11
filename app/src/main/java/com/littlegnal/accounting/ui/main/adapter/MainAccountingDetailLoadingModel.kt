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

import android.view.View
import android.widget.ProgressBar
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

@EpoxyModelClass(layout = R.layout.main_accounting_detail_loading_layout)
abstract class MainAccountingDetailLoadingModel :
    EpoxyModelWithHolder<MainAccountingDetailLoadingModel.MainAccountingDetailLoadingHolder>() {

  class MainAccountingDetailLoadingHolder : EpoxyHolder() {

    var pbLoading: ProgressBar? = null

    override fun bindView(itemView: View?) {
      pbLoading = itemView?.findViewById(R.id.pb_main_accounting_detail_loading)
    }

  }
}