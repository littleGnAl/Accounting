package com.littlegnal.accounting.ui.main

import android.content.DialogInterface
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.BaseFragment
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.MvRxEpoxyController
import com.littlegnal.accounting.base.simpleController
import com.littlegnal.accounting.base.util.success
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.addedit.AddOrEditMvRxStateArgs
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeaderModel
import com.littlegnal.accounting.ui.main.adapter.mainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.mainAccountingDetailHeader
import com.littlegnal.accounting.ui.main.adapter.mainAccountingDetailLoading
import com.littlegnal.accounting.ui.main.adapter.noDataModelView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main.fab_main_add_accounting
import kotlinx.android.synthetic.main.fragment_main.rv_main_detail

class MainFragment : BaseFragment() {

  private val mainMvRxViewModel by activityViewModel(MainViewModel::class)

  private val disposables = CompositeDisposable()

  private var accountingRv: EpoxyRecyclerView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    mainMvRxViewModel.loadFirstPage()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    (activity as MainActivity).updateTitle(getString(R.string.app_name))
    return inflater.inflate(R.layout.fragment_main, container, false).apply {
      accountingRv = findViewById<EpoxyRecyclerView>(R.id.rv_main_detail).apply {
        setController(epoxyController)
        addItemDecoration(DefaultItemDecoration(epoxyController.adapter) {
          it !is MainAccountingDetailHeaderModel
        })
        scrollStateChanges()
            .filter {
              withState(mainMvRxViewModel) { state ->
                !state.isNoMoreData
              }
            }
            .filter {
              withState(mainMvRxViewModel) { state ->
                state.accountingDetailList !is Loading
              }
            }
            .filter { it == RecyclerView.SCROLL_STATE_IDLE }
            .filter {
              val lastPosition = rv_main_detail.adapter?.itemCount?.minus(1) ?: 0
              (rv_main_detail.layoutManager as LinearLayoutManager)
                  .findLastCompletelyVisibleItemPosition() == lastPosition
            }
            .subscribe {
              withState(mainMvRxViewModel) { state ->
                state.success(state.accountingDetailList) { list ->
                  mainMvRxViewModel.loadList(accountingDetailList = list, lastDate = state.lastDate)
                }
              }
            }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onViewCreated(view, savedInstanceState)

    fab_main_add_accounting.setOnClickListener {
      findNavController().navigate(
          R.id.addOrEditFragment,
          Bundle().apply { putParcelable(MvRx.KEY_ARG, AddOrEditMvRxStateArgs(-1)) })
    }

    mainMvRxViewModel.asyncSubscribe(this, MainState::accountingDetailList, true, onFail = {
      activity?.toast(it.message.toString())
    })
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return item?.onNavDestinationSelected(findNavController()) == true ||
        super.onOptionsItemSelected(item)
  }

  override fun onPrepareOptionsMenu(menu: Menu?) {
    super.onPrepareOptionsMenu(menu)

    val menuItem: MenuItem? = menu?.findItem(R.id.summaryFragment)
    withState(mainMvRxViewModel) {
      val isMenuEnabled = it.accountingDetailList()?.isNotEmpty() == true
      if (menuItem?.isEnabled != isMenuEnabled) {
        val resIcon: Drawable? = resources.getDrawable(
            R.drawable.ic_show_chart_black_24dp,
            activity?.theme)
        if (!isMenuEnabled)
          resIcon?.mutate()?.setColorFilter(0xff888888.toInt(), PorterDuff.Mode.SRC_IN)

        menuItem?.isEnabled = isMenuEnabled
        menuItem?.icon = resIcon
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    inflater?.inflate(R.menu.menu, menu)
  }

  override fun invalidate() {
    rv_main_detail.requestModelBuild()
  }

  override fun epoxyController(): MvRxEpoxyController =
    simpleController(mainMvRxViewModel) { state ->
      if (state.isNoData) {
        noDataModelView {
          id("no_data")
        }
      }

      activity?.invalidateOptionsMenu()

      state.accountingDetailList()?.apply {
        for (accountingDetail in this) {
          if (accountingDetail is MainAccountingDetailHeader) {
            mainAccountingDetailHeader {
              id(accountingDetail.title)
              title(accountingDetail.title)
              total(accountingDetail.total)
            }
          }

          if (accountingDetail is MainAccountingDetailContent) {
            mainAccountingDetailContent {
              id(accountingDetail.id)
              time(accountingDetail.time)
              tagName(accountingDetail.tagName)
              remarks(accountingDetail.remarks)
              amount(accountingDetail.amount)
              clickListener { _, _, _, position ->
                val clickContent = this@apply[position] as MainAccountingDetailContent

                findNavController().navigate(
                  R.id.action_mainFragment_to_addOrEditFragment,
                  Bundle().apply {
                    putParcelable(MvRx.KEY_ARG, AddOrEditMvRxStateArgs(clickContent.id))
                  })
              }
              longClickListener { _, _, _, position ->
                val clickContent = this@apply[position] as MainAccountingDetailContent

                showConfirmDeleteDialog(clickContent.id)

                return@longClickListener true
              }
            }
          }
        }
      }

      if (state.accountingDetailList is Loading) mainAccountingDetailLoading { id("loading") }
    }

  private fun showConfirmDeleteDialog(deleteId: Int) {
    val tag = "DeleteConfirmDialog"
    val dialog: DialogFragment = DeleteConfirmDialog().apply {
      okClickListener = DialogInterface.OnClickListener { _, _ ->
        withState(mainMvRxViewModel) { state ->
          state.success(state.accountingDetailList) {
            mainMvRxViewModel.deleteAccounting(it, deleteId)
          }
        }
      }
    }
    val ft = activity?.supportFragmentManager?.beginTransaction()
    val preF = activity?.supportFragmentManager?.findFragmentByTag(tag)
    if (preF != null) {
      ft?.remove(preF)
    }
    ft?.addToBackStack(null)
    ft?.commit()

    dialog.show(activity?.supportFragmentManager, tag)
  }

  override fun onDestroyView() {
    accountingRv?.clearOnScrollListeners()
    accountingRv = null
    disposables.dispose()
    super.onDestroyView()
  }
}
