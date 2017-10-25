package com.littlegnal.accounting.db

import android.arch.persistence.room.ColumnInfo

/**
 * @author littlegnal
 * @date 2017/10/9
 */
data class TagAndTotal(
    @ColumnInfo(name = "tag_name") var tagName: String,
    @ColumnInfo(name = "total") var total: Float)