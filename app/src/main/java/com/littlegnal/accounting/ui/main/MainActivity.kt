package com.littlegnal.accounting.ui.main

import android.content.DialogInterface
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.mvi.BaseMviActivity
import com.littlegnal.accounting.ui.addedit.AddOrEditActivity
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailController
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeaderModel
import com.littlegnal.accounting.ui.summary.SummaryActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity :
    BaseMviActivity<MainView, MainPresenter>(),
    MainView {

  @Inject
  lateinit var mainPresenter: MainPresenter

  private lateinit var accountingDetailController: MainAccountingDetailController
  private lateinit var layoutManager: LinearLayoutManager

  private val disposables = CompositeDisposable()

  private val deleteItemPublisher: PublishSubject<Int> = PublishSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    title = getString(R.string.app_name)

    accountingDetailController = MainAccountingDetailController()
    val adapter = accountingDetailController.adapter
    rv_main_detail.adapter = adapter
    layoutManager = LinearLayoutManager(this)
    rv_main_detail.layoutManager = layoutManager
    rv_main_detail.addItemDecoration(
        DefaultItemDecoration(adapter) { it !is MainAccountingDetailHeaderModel })

    toolbar.setOnMenuItemClickListener {
      if (it?.itemId == R.id.menu_summary) {
        SummaryActivity.go(this)
        return@setOnMenuItemClickListener true
      }
      return@setOnMenuItemClickListener false
    }

    fab_main_add_accounting.setOnClickListener { AddOrEditActivity.add(this) }

    disposables.add(accountingDetailController.getItemClickObservable()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { AddOrEditActivity.edit(this, it) })

    disposables.add(accountingDetailController.getItemLongClickObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { id ->
          val tag = "DeleteConfirmDialog"
          val dialog: DialogFragment = DeleteConfirmDialog().apply {
            okClickListener = DialogInterface.OnClickListener { _, _ ->
              deleteItemPublisher.onNext(id)
            }
          }
          val ft = supportFragmentManager.beginTransaction()
          val preF = supportFragmentManager.findFragmentByTag(tag)
          if (preF != null) {
            ft.remove(preF)
          }
          ft.addToBackStack(null)
          ft.commit()

          dialog.show(supportFragmentManager, tag)
        })
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    val menuItem: MenuItem? = menu?.findItem(R.id.menu_summary)
    val isMenuEnabled = accountingDetailController.accountingDetailList?.isNotEmpty()
    if ((menuItem?.isEnabled == true && isMenuEnabled == false) ||
        (menuItem?.isEnabled == false && isMenuEnabled == true)) {
      val resIcon: Drawable? = resources?.getDrawable(R.drawable.ic_show_chart_black_24dp, theme)
      if (isMenuEnabled == false)
        resIcon?.mutate()?.setColorFilter(0xff888888.toInt(), PorterDuff.Mode.SRC_IN)

      menuItem.isEnabled = isMenuEnabled
      menuItem.icon = resIcon
      return true
    }

    return super.onPrepareOptionsMenu(menu)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun loadFirstPageIntent(): Observable<Boolean> {
    return Observable.just(true)
  }

  override fun loadNextPageIntent(): Observable<Date> {
    return RxRecyclerView.scrollStateChanges(rv_main_detail)
        .filter { !accountingDetailController.isNoMoreData }
        .filter { !accountingDetailController.isLoadingMore }
        .filter { it == RecyclerView.SCROLL_STATE_IDLE }
        .filter { layoutManager.findLastCompletelyVisibleItemPosition() ==
            accountingDetailController.adapter.itemCount - 1 }
        .map { accountingDetailController.lastDate }
  }

  override fun deleteAccountingIntent(): Observable<Int> = deleteItemPublisher

  override fun createPresenter(): MainPresenter = mainPresenter

  override fun render(viewState: MainViewState) {
    when {
      viewState.isNoData -> renderNoDataState(viewState)
      viewState.isLoading -> renderLoadingState(viewState)
      viewState.error != null -> renderErrorState(viewState)
      else -> renderDataState(viewState)
    }
  }

  private fun renderNoDataState(vs: MainViewState) {
    tv_main_accounting_no_data.visibility = View.VISIBLE
    accountingDetailController.setData(vs.accountingDetailList, false)
  }

  private fun renderLoadingState(vs: MainViewState) {
    if (tv_main_accounting_no_data.visibility == View.VISIBLE) {
      tv_main_accounting_no_data.visibility = View.GONE
    }

    accountingDetailController.setData(vs.accountingDetailList, true)
  }

  private fun renderDataState(vs: MainViewState) {
    if (tv_main_accounting_no_data.visibility == View.VISIBLE) {
      tv_main_accounting_no_data.visibility = View.GONE
    }

    invalidateOptionsMenu()
    accountingDetailController.setData(vs.accountingDetailList, false)
    accountingDetailController.lastDate = vs.lastDate
    accountingDetailController.isNoMoreData = vs.isNoMoreData
  }

  private fun renderErrorState(vs: MainViewState) {
    Toast.makeText(this, vs.error, Toast.LENGTH_SHORT).show()
  }

  override fun onDestroy() {
    super.onDestroy()

    disposables.dispose()
  }
}
