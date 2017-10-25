package com.littlegnal.accounting.ui.summary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.mvi.BaseMviActivity
import com.littlegnal.accounting.ui.summary.adapter.SummaryListController
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItemModel
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_summary.*
import java.util.*
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/9/26
 */
class SummaryActivity : BaseMviActivity<SummaryView, SummaryPresenter>(), SummaryView {

  @Inject
  lateinit var summaryPresenter: SummaryPresenter

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

  }

  override fun loadDataIntent(): Observable<Boolean> {
    return Observable.just(true)
  }

  override fun monthClickedIntent(): Observable<Date> {
    // 点击曲线图的点
    return cv_summary_chart.getMonthClickedObservable()
  }

  override fun render(viewState: SummaryViewState) {
    // 根据不同的State来展示界面
    when(viewState) {
      is SummaryViewState.SummaryDataViewState -> renderDataState(viewState)
      is SummaryViewState.SummaryGroupingTagViewState -> renderGroupingTagState(viewState)
    }
  }

  private fun renderGroupingTagState(vs: SummaryViewState.SummaryGroupingTagViewState) {
    summaryListController.setData(vs.summaryItemList)
  }

  private fun renderDataState(vs: SummaryViewState.SummaryDataViewState) {
    // 曲线图赋值
    cv_summary_chart.points = vs.points
    cv_summary_chart.months = vs.months
    cv_summary_chart.values = vs.values
    cv_summary_chart.selectedIndex = vs.selectedIndex
    cv_summary_chart.postInvalidate()
    // 标签汇总列表赋值
    summaryListController.setData(vs.summaryItemList)
  }

  override fun createPresenter(): SummaryPresenter = summaryPresenter

  companion object {
    fun go(activity: Activity) {
      activity.startActivity(Intent(activity, SummaryActivity::class.java))
    }
  }
}