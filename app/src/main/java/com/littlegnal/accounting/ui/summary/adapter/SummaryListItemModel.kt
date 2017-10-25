package com.littlegnal.accounting.ui.summary.adapter

import android.support.v7.widget.AppCompatTextView
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

/**
 * @author littlegnal
 * @date 2017/10/6
 */
@EpoxyModelClass(layout = R.layout.summary_list_item_layout)
abstract class SummaryListItemModel :
    EpoxyModelWithHolder<SummaryListItemModel.SummaryListItemHolder>() {

  @EpoxyAttribute var tag: String? = null
  @EpoxyAttribute var total: String? = null

  override fun bind(holder: SummaryListItemHolder?) {
    holder?.tag?.text = tag
    holder?.total?.text = total
  }

  class SummaryListItemHolder : EpoxyHolder() {

    var tag: AppCompatTextView? = null
    var total: AppCompatTextView? = null

    override fun bindView(itemView: View?) {
      tag = itemView?.findViewById(R.id.tv_summary_list_item_tag)
      total = itemView?.findViewById(R.id.tv_summary_list_item_total)

    }

  }
}