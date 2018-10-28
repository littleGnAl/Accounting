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

package com.littlegnal.accounting.base.mvi

/**
 * 像弹窗，toast或者动画等，很多时候我们只需要显示一次，所以通过该状态来控制
 * * [SHOW] : 显示
 * * [NONE] : 不显示
 */
enum class UiNotificationStatus {
  SHOW,
  NONE
}
