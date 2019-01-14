package com.littlegnal.accounting.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.main.MainActivity
import com.littlegnal.accounting.ui.summary.adapter.SummaryListController
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItemModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_summary.cv_summary_chart
import kotlinx.android.synthetic.main.fragment_summary.rv_summary_list

class SummaryFragment : BaseMvRxFragment() {

  private val summaryViewModel: SummaryViewModel by fragmentViewModel()

  private val summaryListController: SummaryListController by lazy { SummaryListController() }

  private val disposables by lazy { CompositeDisposable() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    summaryViewModel.initiate()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    (activity as MainActivity).updateTitle(getString(R.string.summary_title))
    return inflater.inflate(R.layout.fragment_summary, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    rv_summary_list.adapter = summaryListController.adapter
    rv_summary_list.layoutManager = LinearLayoutManager(activity)
    rv_summary_list.addItemDecoration(
        DefaultItemDecoration(summaryListController.adapter) { it is SummaryListItemModel })

    disposables += cv_summary_chart.getMonthClickedObservable()
        .subscribe {
          summaryViewModel.switchMonth(it)
        }
  }

  override fun invalidate() {
    withState(summaryViewModel) { state ->
      if (state.error != null) {
        activity?.toast(state.error.message.toString())
        return@withState
      }

      if (!state.isLoading) {
        summaryListController.setData(state.summaryItemList())
        cv_summary_chart.summaryChartData = state.summaryChartData()!!
      }
    }
  }
}
