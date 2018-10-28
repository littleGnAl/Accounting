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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import io.reactivex.Maybe
import java.util.Date

@Dao
interface AccountingDao {

  @Query(
      """
    SELECT * FROM accounting WHERE createTime <= :lastDate ORDER BY createTime DESC
    LIMIT :limit
    """
  )
  fun queryPreviousAccounting(
    lastDate: Date,
    limit: Long
  ): Maybe<List<Accounting>>

  /**
   * @param someDayDate 日期格式为`yyyy-MM-dd`
   */
  @Deprecated("This will case bug")
  @Query(
      """
    SELECT SUM(amount)
    FROM accounting
    WHERE datetime(createTime / 1000, 'unixepoch') >= date(:someDayDate) AND
      datetime(createTime / 1000, 'unixepoch') < date(:someDayDate, '+1 day')
    """
  )
  fun sumOfDay(someDayDate: String): Float

  @Query(
      """
    SELECT SUM(amount)
    FROM accounting
    WHERE datetime(createTime / 1000, 'unixepoch')
    BETWEEN datetime(:timeInMillis, 'unixepoch')
    AND datetime(:timeInMillis + 60 * 60 * 24, 'unixepoch')
    """
  )
  fun sumOfDay(timeInMillis: Long): Float

  @Query("SELECT * FROM accounting WHERE id = :id")
  fun getAccountingById(id: Int): Maybe<Accounting>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAccounting(accounting: Accounting): Long

  @Query(
      """
    SELECT SUM(amount) as total, tag_name
    FROM accounting
    WHERE strftime('%Y', createTime / 1000, 'unixepoch') = :year
      AND  strftime('%m', createTime / 1000, 'unixepoch') = :month
    GROUP BY tag_name
    """
  )
  fun getGroupingMonthTotalAmountObservable(
    year: String,
    month: String
  ): Maybe<List<TagAndTotal>>

  @Deprecated("This will no long used")
  @Query(
      """
    SELECT SUM(amount) as total, tag_name
    FROM accounting
    WHERE strftime('%Y', createTime / 1000, 'unixepoch') = :year
      AND  strftime('%m', createTime / 1000, 'unixepoch') = :month
    GROUP BY tag_name
    """
  )
  fun getGroupingMonthTotalAmount(
    year: String,
    month: String
  ): List<TagAndTotal>

  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query(
      """
    SELECT SUM(amount) as total, tag_name, strftime('%Y-%m', createTime / 1000, 'unixepoch') year_month
    FROM accounting
    GROUP BY tag_name
    ORDER BY year_month DESC
    LIMIT 1
    """
  )
  fun getLastGroupingMonthTotalAmountObservable(): Maybe<List<TagAndTotal>>

  @Query(
      """
    SELECT strftime('%Y-%m', createTime / 1000, 'unixepoch') year_month, SUM(amount) total
    FROM accounting
    GROUP BY year_month
    ORDER BY year_month DESC
    LIMIT :limit
    """
  )
  fun getMonthTotalAmount(limit: Long): Maybe<List<MonthTotal>>

  @Query(
      """
    DELETE FROM accounting WHERE id = :id
    """
  )
  fun deleteAccountingById(id: Int): Int
}
