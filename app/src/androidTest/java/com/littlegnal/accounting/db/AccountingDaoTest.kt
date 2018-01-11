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

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class AccountingDaoTest {

  private lateinit var appDataBase: AppDataBase

  private lateinit var accountingDao: AccountingDao

  @Before
  fun initDb() {
    appDataBase = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getContext(),
        AppDataBase::class.java)
        .allowMainThreadQueries()
        .build()
    accountingDao = appDataBase.accountingDao()
  }

  @After
  fun closeDb() {
    appDataBase.close()
  }

  @Test
  fun test_getAccountingById_whenNoUsers() {
    accountingDao.getAccountingById(1)
        .test()
        .assertNoValues()

    accountingDao.getAccountingById(10)
        .test()
        .assertNoValues()

    accountingDao.getAccountingById(100)
        .test()
        .assertNoValues()
  }

  @Test
  fun test_insertAndGetUser() {
    val account = Accounting(
        100.0f,
        Calendar.getInstance().time,
        "早餐",
        "100块的茶叶蛋")
    val insertedId = accountingDao.insertAccounting(account)
    account.id = insertedId.toInt()

    accountingDao.getAccountingById(insertedId.toInt())
        .test()
        .assertValue {
          it.id == account.id &&
              it.amount == account.amount &&
              it.createTime == account.createTime &&
              it.tagName == account.tagName &&
              it.remarks == account.remarks
        }
  }

  @Test
  fun test_sumOfDay() {
    val todayCalendar = Calendar.getInstance()
    val account1 = Accounting(
        100.0f,
        todayCalendar.time,
        "早餐",
        "100块的茶叶蛋")
    todayCalendar.add(Calendar.HOUR_OF_DAY, -1)
    val account2 = Accounting(
        100.0f,
        todayCalendar.time,
        "早餐",
        "100块的茶叶蛋")
    todayCalendar.add(Calendar.HOUR_OF_DAY, -1)
    val account3 = Accounting(
        100.0f,
        todayCalendar.time,
        "早餐",
        "100块的茶叶蛋")

    accountingDao.insertAccounting(account1)
    accountingDao.insertAccounting(account2)
    accountingDao.insertAccounting(account3)

    val amountSum = accountingDao.sumOfDay(ONE_DAY_FORMAT.format(todayCalendar.time))
    assertThat(amountSum, Matchers.`is`(300.0f))
  }

  @Test
  fun test_deleteAccountingById() {
    val account = Accounting(
        100.0f,
        Calendar.getInstance().time,
        "早餐",
        "100块的茶叶蛋")
    val insertedId: Int = accountingDao.insertAccounting(account).toInt()
    account.id = insertedId

    accountingDao.getAccountingById(insertedId)
        .test()
        .assertValue {
          it.id == account.id &&
              it.amount == account.amount &&
              it.createTime == account.createTime &&
              it.tagName == account.tagName &&
              it.remarks == account.remarks
        }

    accountingDao.deleteAccountingById(insertedId)

    accountingDao.getAccountingById(insertedId)
        .test()
        .assertNoValues()
  }

  @Test
  fun test_queryPreviousAccounting() {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    calendar.add(Calendar.MONTH, -1)
    val account2 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")

    accountingDao.insertAccounting(account1)
    accountingDao.insertAccounting(account2)

    accountingDao.queryPreviousAccounting(Calendar.getInstance().time, 100)
        .test()
        .assertValue { it.size == 2}
  }

  @Test
  fun test_getGroupingMonthTotalAmountObservable() {
    val today = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    calendar.add(Calendar.HOUR_OF_DAY, -1)
    val account2 = Accounting(
        100.0f,
        calendar.time,
        "午餐",
        "100块的鸭腿")
    accountingDao.insertAccounting(account1)
    accountingDao.insertAccounting(account2)

    val tagTotal1 = TagAndTotal("早餐", 100.0f)
    val tagTotal2 = TagAndTotal("午餐", 100.0f)

    accountingDao.getGroupingMonthTotalAmountObservable(
        today.get(Calendar.YEAR).toString(),
        ensureNum2Length(today.get(Calendar.MONTH) + 1))
        .test()
        .assertValue { it.size == 2 && it.containsAll(listOf(tagTotal1, tagTotal2)) }
  }

  @Test
  fun test_getGroupingMonthTotalAmount() {
    val today = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    calendar.add(Calendar.HOUR_OF_DAY, -1)
    val account2 = Accounting(
        100.0f,
        calendar.time,
        "午餐",
        "100块的鸭腿")
    accountingDao.insertAccounting(account1)
    accountingDao.insertAccounting(account2)

    val tagTotal1 = TagAndTotal("早餐", 100.0f)
    val tagTotal2 = TagAndTotal("午餐", 100.0f)

    val resultList = accountingDao.getGroupingMonthTotalAmount(
        today.get(Calendar.YEAR).toString(),
        ensureNum2Length((today.get(Calendar.MONTH) + 1)))

    assertThat(resultList.size, Matchers.`is`(2))
    assertThat(resultList.containsAll(listOf(tagTotal1, tagTotal2)), Matchers.`is`(true))
  }

  @Test
  fun test_getMonthTotalAmount() {
    val calendar = Calendar.getInstance()
    val yearMonthFormat1 = YEAR_MONTH_FORMAT.format(calendar.time)
    val account1 = Accounting(
        100.0f,
        calendar.time,
        "早餐",
        "100块的茶叶蛋")
    calendar.add(Calendar.MONTH, -1)
    val yearMonthFormat2 = YEAR_MONTH_FORMAT.format(calendar.time)
    val account2 = Accounting(
        100.0f,
        calendar.time,
        "午餐",
        "100块的鸭腿")
    accountingDao.insertAccounting(account1)
    accountingDao.insertAccounting(account2)

    val monthTotal1 = MonthTotal(yearMonthFormat1, 100.0f)
    val monthTotal2 = MonthTotal(yearMonthFormat2, 100.0f)

    accountingDao.getMonthTotalAmount(100)
        .test()
        .assertValue { it.size == 2 && it.containsAll(listOf(monthTotal1, monthTotal2)) }
  }

  companion object {

    private fun ensureNum2Length(num: Int): String =
        if (num < 10) {
          "0$num"
        } else {
          num.toString()
        }

    private val ONE_DAY_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val YEAR_MONTH_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM")
  }

}