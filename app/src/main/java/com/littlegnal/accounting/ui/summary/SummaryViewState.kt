package com.littlegnal.accounting.ui.summary

import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import java.util.*

/**
 * @author littlegnal
 * @date 2017/9/26
 */
sealed class SummaryViewState : MviViewState {

  /**
   * 默认显示曲线图和标签汇总状态（首次进入页面）
   */
  data class SummaryDataViewState(
      val points: List<Pair<Int, Float>>, // 曲线图点
      val months: List<Pair<String, Date>>, // 曲线图月份
      val values: List<String>, // 曲线图数值文本
      val selectedIndex: Int, // 曲线图选中月份索引
      val summaryItemList: List<SummaryListItem> // 当月标签汇总列表
  ) : SummaryViewState()

  /**
   * 切换月份时标签汇总状态
   */
  data class SummaryGroupingTagViewState(
      val summaryItemList: List<SummaryListItem> // 当月标签汇总列表
  ) : SummaryViewState()
}