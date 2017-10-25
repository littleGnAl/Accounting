package com.littlegnal.accounting.ui.summary

import com.littlegnal.accounting.base.mvi.MviView
import io.reactivex.Observable
import java.util.*

/**
 * @author littlegnal
 * @date 2017/9/26
 */
interface SummaryView : MviView<SummaryViewState> {

  /**
   * 首次加载页面
   */
  fun loadDataIntent(): Observable<Boolean>

  /**
   * 切换月份
   */
  fun monthClickedIntent(): Observable<Date>
}