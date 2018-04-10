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

import com.littlegnal.accounting.base.mvi.MviIntent
import com.littlegnal.accounting.db.Accounting
import java.util.Date

sealed class MainIntent : MviIntent {

  /**
   * 初始化
   */
  class InitialIntent : MainIntent()

  /**
   * 加载下一页
   */
  data class LoadNextPageIntent(val lastDate: Date) : MainIntent()

  /**
   * 删除某一项记录
   */
  data class DeleteAccountingIntent(val deletedId: Int) : MainIntent()

  /**
   * 添加或编辑某一项记录
   */
  data class AddOrEditAccountingIntent(
    val isAddedAccounting: Boolean,
    val accounting: Accounting
  ) : MainIntent()
}