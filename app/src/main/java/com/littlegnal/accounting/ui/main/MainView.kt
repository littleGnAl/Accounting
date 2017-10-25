package com.littlegnal.accounting.ui.main

import com.littlegnal.accounting.base.mvi.MviView
import io.reactivex.Observable
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/7
 */
interface MainView: MviView<MainViewState> {

  /**
   * 加载第一页
   */
  fun loadFirstPageIntent(): Observable<Boolean>

  /**
   * 加载下一页
   */
  fun loadNextPageIntent(): Observable<Date>

  /**
   * 删除某一项记录
   */
  fun deleteAccountingIntent(): Observable<Int>
}