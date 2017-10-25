package com.littlegnal.accounting.ui.main

import com.littlegnal.accounting.db.Accounting
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/14
 */
sealed class MainPartialStateChanges {

  /**
   * 错误信息
   */
  data class ErrorPartialState(val error: String?) : MainPartialStateChanges()

  /**
   * 加载第一页的结果
   */
  data class LoadFirstPagePartialState(
      val accountingList: List<Accounting>) : MainPartialStateChanges()

  /**
   * 加载下一页的结果
   */
  data class LoadNextPagePartialState(
      val lastDate: Date,
      val accountingList: List<Accounting>) : MainPartialStateChanges()

  /**
   * 增/更新
   */
  data class AddOrUpdatePartialState(val accounting: Accounting) : MainPartialStateChanges()

  /**
   * 删除某一项的结果
   */
  data class DeleteAccountingPartialState(val deletedId: Int) : MainPartialStateChanges()

  /**
   * loading状态
   */
  object LoadingPartialState: MainPartialStateChanges()

}