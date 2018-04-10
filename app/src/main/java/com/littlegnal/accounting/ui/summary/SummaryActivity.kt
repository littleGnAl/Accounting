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

package com.littlegnal.accounting.ui.summary

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.BaseActivity
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.mvi.MviView
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.summary.adapter.SummaryListController
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItemModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_summary.cv_summary_chart
import kotlinx.android.synthetic.main.activity_summary.rv_summary_list
import javax.inject.Inject

/**
 * 记帐记录汇总页面
 */
class SummaryActivity : BaseActivity(), MviView<SummaryIntent, SummaryViewState> {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  private lateinit var summaryViewModel: SummaryViewModel

  private val disposables = CompositeDisposable()

  private lateinit var summaryListController: SummaryListController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_summary)
    setToolbarWithBack()
    setTitle(R.string.summary_title)

    summaryListController = SummaryListController()
    rv_summary_list.adapter = summaryListController.adapter
    rv_summary_list.layoutManager = LinearLayoutManager(this)
    rv_summary_list.addItemDecoration(
        DefaultItemDecoration(summaryListController.adapter) { it is SummaryListItemModel })

    bind()
  }

  private fun bind() {
    summaryViewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(SummaryViewModel::class.java)

    // 订阅render方法根据发送过来的state渲染界面
    disposables += summaryViewModel.states()
        .subscribe(this::render)
    // 传递UI的intents给ViewModel
    summaryViewModel.processIntents(intents())
  }

  private fun initialIntent(): Observable<SummaryIntent> {
    return Observable.just(SummaryIntent.InitialIntent())
  }

  private fun switchMonthIntent(): Observable<SummaryIntent> {
    // 点击曲线图的点
    return cv_summary_chart.getMonthClickedObservable()
        .map { SummaryIntent.SwitchMonthIntent(it) }
  }

  override fun render(state: SummaryViewState) {
    if (state.error != null) {
      toast(state.error.toString())
      return
    }
    // 标签汇总列表赋值
    summaryListController.setData(state.summaryItemList)
    if (state.isSwitchMonth) return
    // 曲线图赋值
    cv_summary_chart.points = state.points
    cv_summary_chart.months = state.months
    cv_summary_chart.values = state.values
    cv_summary_chart.selectedIndex = state.selectedIndex
    cv_summary_chart.postInvalidate()
  }

  override fun intents(): Observable<SummaryIntent> {
    return Observable.merge(initialIntent(), switchMonthIntent())
  }

  companion object {
    fun go(activity: Activity) {
      activity.startActivity(Intent(activity, SummaryActivity::class.java))
    }
  }
}