package com.littlegnal.accounting.ui.main.adapter

import android.view.View
import android.widget.ProgressBar
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

/**
 * @author littlegnal
 * @date 2017/10/12
 */
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