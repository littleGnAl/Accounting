package com.littlegnal.accounting.ui.summary.adapter

import com.airbnb.epoxy.TypedEpoxyController

/**
 * @author littlegnal
 * @date 2017/10/6
 */
class SummaryListController : TypedEpoxyController<List<SummaryListItem>>() {
  override fun buildModels(data: List<SummaryListItem>?) {
    data?.let {
      for (item in data) {
//        SummaryListItemModel_(item.tagName, item.total)
//            .id(item.tagName)
//            .addTo(this)

        summaryListItem {
          id(item.tagName)
          tag(item.tagName)
          total(item.total)
        }
      }
    }
  }
}