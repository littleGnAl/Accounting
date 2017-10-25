package com.littlegnal.accounting.db

import android.arch.persistence.room.TypeConverter
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/11
 */
class Converters {

  @TypeConverter
  fun fromTimestamp(value: Long?): Date? = if (value != null) Date(value) else null

  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? = date?.time
}