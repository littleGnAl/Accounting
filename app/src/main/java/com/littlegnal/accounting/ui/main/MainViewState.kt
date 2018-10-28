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

package com.littlegnal.accounting.ui.main

import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail

/**
 * 主页[MviViewState]
 */
data class MainViewState(
  val error: Throwable?, // 错误信息
  val isLoading: Boolean, // 是否正在加载
  val accountingDetailList: List<MainAccountingDetail>, // 列表数据
  val isNoData: Boolean = false, // 用于表示加载第一页的时候是否本地无数据
  val isNoMoreData: Boolean // 用于表示加载下一页数据的时候是否已经没有更多数据
) : MviViewState {
  companion object {

    /**
     * 初始[MainViewState]用于Reducer
     */
    fun idle() = MainViewState(
        null,
        false,
        listOf(),
        false,
        false
    )
  }
}
