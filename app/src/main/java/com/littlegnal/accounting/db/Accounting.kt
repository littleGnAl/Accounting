package com.littlegnal.accounting.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/9
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
    return "Accounting(amount=$amount, createTime=$createTime, " +
        "tagName='$tagName', remarks=$remarks, id=$id)"
  }


}