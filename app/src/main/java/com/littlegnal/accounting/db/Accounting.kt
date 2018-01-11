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

package com.littlegnal.accounting.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * 记帐表
 */
@Entity(tableName = "accounting")
data class Accounting(
    @ColumnInfo(name = "amount") var amount: Float,
    @ColumnInfo(name = "createTime") var createTime: Date,
    @ColumnInfo(name = "tag_name") var tagName: String,
    @ColumnInfo(name = "remarks") var remarks: String?) {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  var id: Int = 0

  override fun toString(): String {
    return "Accounting(amount=$amount, createTime=$createTime, tagName='$tagName', " +
        "remarks=$remarks, id=$id)"
  }
}