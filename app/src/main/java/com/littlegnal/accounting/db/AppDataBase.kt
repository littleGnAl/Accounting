package com.littlegnal.accounting.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.littlegnal.accounting.base.DB_VERSION

/**
 * @author littlegnal
 * @date 2017/8/9
 */
@Database(entities = arrayOf(Accounting::class), version = DB_VERSION)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
  abstract fun accountingDao(): AccountingDao
}