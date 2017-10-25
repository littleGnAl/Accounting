package com.littlegnal.accounting.ui.main.adapter

import android.support.constraint.ConstraintLayout
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
@EpoxyModelClass(layout = R.layout.main_accounting_detail_content_layout)
abstract class MainAccountingDetailContentModel :
    EpoxyModelWithHolder<MainAccountingDetailContentModel.ContentModel>() {

  @EpoxyAttribute var time: String? = null
  @EpoxyAttribute var tagName: String? = null
  @EpoxyAttribute var remarks: String? = null
  @EpoxyAttribute var amount: String? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash) var clickListener: View.OnClickListener? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var longClickListener: View.OnLongClickListener? = null

  override fun bind(holder: ContentModel?) {
    super.bind(holder)

    holder?.tvTime?.text = time
    holder?.tvTag?.text = tagName
    holder?.tvRemarks?.text = remarks
    holder?.tvAmount?.text = amount
    holder?.parent?.setOnClickListener(clickListener)
    holder?.parent?.setOnLongClickListener(longClickListener)
  }

  class ContentModel : EpoxyHolder() {
    lateinit var parent: ConstraintLayout
    lateinit var tvTime: AppCompatTextView
    lateinit var tvTag: AppCompatTextView
    lateinit var tvRemarks: AppCompatTextView
    lateinit var tvAmount: AppCompatTextView

    override fun bindView(itemView: View?) {
      itemView?.let {
        parent = it.findViewById(R.id.main_accounting_detail_header_parent)
        tvTime = it.findViewById(R.id.tv_main_accounting_detail_content_time)
        tvTag = it.findViewById(R.id.tv_main_accounting_detail_content_tag_name)
        tvRemarks = it.findViewById(R.id.tv_main_accounting_detail_content_remarks)
        tvAmount = it.findViewById(R.id.tv_main_accounting_detail_content_amount)
      }

    }

  }
}