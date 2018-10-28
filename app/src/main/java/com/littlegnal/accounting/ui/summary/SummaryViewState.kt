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

package com.littlegnal.accounting.ui.summary

import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItem
import java.util.Date

data class SummaryViewState(
  val isLoading: Boolean, // 是否正在加载
  val error: Throwable?, // 错误信息
  val points: List<Pair<Int, Float>>, // 曲线图点
  val months: List<Pair<String, Date>>, // 曲线图月份
  val values: List<String>, // 曲线图数值文本
  val selectedIndex: Int, // 曲线图选中月份索引
  val summaryItemList: List<SummaryListItem>, // 当月标签汇总列表
  val isSwitchMonth: Boolean // 是否切换月份
) : MviViewState {
  companion object {

    /**
     * 初始[SummaryViewState]用于Reducer
     */
    fun idle() = SummaryViewState(false, null, listOf(), listOf(), listOf(), 0, listOf(), false)
  }
}
