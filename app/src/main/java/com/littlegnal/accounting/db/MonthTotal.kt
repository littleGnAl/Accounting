package com.littlegnal.accounting.db

import androidx.room.ColumnInfo

/**
 * 月汇总数据库实体
 *
 * @see [AccountingDao.getMonthTotalAmount]
 */
data class MonthTotal(
  @ColumnInfo(name = "year_month") var yearAndMonth: String,
  @ColumnInfo(name = "total") var total: Float
)
