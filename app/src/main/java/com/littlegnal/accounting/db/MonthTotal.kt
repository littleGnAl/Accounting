package com.littlegnal.accounting.db

import android.arch.persistence.room.ColumnInfo

/**
 * @author littlegnal
 * @date 2017/10/9
 */
data class MonthTotal(
    @ColumnInfo(name = "year_month") var yearAndMonth: String,
    @ColumnInfo(name = "total")  var total: Float)