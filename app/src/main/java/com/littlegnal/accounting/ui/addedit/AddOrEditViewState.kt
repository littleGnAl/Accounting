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

package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvi.MviViewState

/**
 * 增加或修改页面状态
 */
data class AddOrEditViewState(
  val isLoading: Boolean, // 是否正在加载
  val error: Throwable?, // 错误信息
  val amount: String? = null, // 金额
  val tagName: String? = null, // 标签
  val dateTime: String? = null, // 日期时间
  val remarks: String? = null, // 备注
  val isNeedFinish: Boolean = false // 是否需要关闭页面
) : MviViewState {
  companion object {

    /**
     * 初始[AddOrEditViewState]用于Reducer
     */
    fun idle() = AddOrEditViewState(false, null)
  }
}
