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

import com.littlegnal.accounting.base.mvi.MviIntent

sealed class AddOrEditIntent : MviIntent {

  /**
   * 初始化
   */
  data class InitialIntent(val accountingId: Int?) : AddOrEditIntent()

  /**
   * 创建或者更新记录
   */
  data class CreateOrUpdateIntent(
      val accountingId: Int?,
      val amount: Float,
      val tagName: String,
      val showDate: String,
      val remarks: String?
  ) : AddOrEditIntent()
}