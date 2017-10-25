package com.littlegnal.accounting.base

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyModel
import com.littlegnal.accounting.base.util.dip

/**
 * @author littlegnal
 * @date 2017/10/9
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