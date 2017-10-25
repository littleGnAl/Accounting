package com.littlegnal.accounting.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/9
 */
@Dao
interface AccountingDao {

  @Query("""
    SELECT * FROM accounting WHERE createTime <= :lastDate ORDER BY createTime DESC
    LIMIT :limit
    """)
  fun queryPreviousAccounting(lastDate: Date, limit: Long): Maybe<List<Accounting>>

  /**
   * @param someDayDate 日期格式为`yyyy-MM-dd`
   */
  @Query("""
    SELECT SUM(amount)
    FROM accounting
    WHERE datetime(createTime / 1000, 'unixepoch') >= date(:someDayDate) AND
      datetime(createTime / 1000, 'unixepoch') < date(:someDayDate, '+1 day')
    """)
  fun sumOfDay(someDayDate: String): Float

  @Query("SELECT * FROM accounting WHERE id = :id")
  fun getAccountingById(id: Int): Maybe<Accounting>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAccounting(accounting: Accounting): Long

  @Query("""
    SELECT SUM(amount) as total, tag_name
    FROM accounting
    WHERE strftime('%Y', createTime / 1000, 'unixepoch') = :year
      AND  strftime('%m', createTime / 1000, 'unixepoch') = :month
    GROUP BY tag_name
    """)
  fun getGroupingMonthTotalAmountObservable(year: String, month: String): Maybe<List<TagAndTotal>>

  @Query("""
    SELECT SUM(amount) as total, tag_name
    FROM accounting
    WHERE strftime('%Y', createTime / 1000, 'unixepoch') = :year
      AND  strftime('%m', createTime / 1000, 'unixepoch') = :month
    GROUP BY tag_name
    """)
  fun getGroupingMonthTotalAmount(year: String, month: String): List<TagAndTotal>

  @Query("""
    SELECT strftime('%Y-%m', createTime / 1000, 'unixepoch') year_month, SUM(amount) total
    FROM accounting
    GROUP BY year_month
    ORDER BY year_month DESC
    LIMIT :limit
    """)
  fun getMonthTotalAmount(limit: Long): Maybe<List<MonthTotal>>

  @Query("""
    DELETE FROM accounting WHERE id = :id
    """)
  fun deleteAccountingById(id: Int): Int
}