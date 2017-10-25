package com.littlegnal.accounting.ui.main.adapter

import android.support.v7.widget.AppCompatTextView
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.littlegnal.accounting.R

/**
 * @author littlegnal
 * @date 2017/8/23
 */
@EpoxyModelClass(layout = R.layout.main_accounting_detail_header_layout)
abstract class MainAccountingDetailHeaderModel :
    EpoxyModelWithHolder<MainAccountingDetailHeaderModel.HeaderHolder>() {

  @EpoxyAttribute var title: String? = null
  @EpoxyAttribute var total: String? = null

  override fun bind(holder: HeaderHolder?) {
    super.bind(holder)

    holder?.header?.text = title
    holder?.total?.text = total
  }

  class HeaderHolder : EpoxyHolder() {

    lateinit var header: AppCompatTextView
    lateinit var total: AppCompatTextView

    override fun bindView(itemView: View?) {
      itemView?.let {
        header = itemView.findViewById(R.id.main_accounting_detail_header_title)
        total = itemView.findViewById(R.id.main_accounting_detail_header_total)
      }
    }
  }
}