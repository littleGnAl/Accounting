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

package com.littlegnal.accounting.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
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
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.BaseActivity
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.mvi.MviView
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.addedit.AddOrEditActivity
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailController
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeaderModel
import com.littlegnal.accounting.ui.summary.SummaryActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 主页，按每天来分组记帐记录，展示记帐记录列表
 */
class MainActivity : BaseActivity(), MviView<MainIntent, MainViewState> {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  private lateinit var mainViewModel: MainViewModel

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

    disposables += accountingDetailController.getItemClickObservable()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { AddOrEditActivity.edit(this, it) }

    disposables += accountingDetailController.getItemLongClickObservable()
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
        }

    bind()
  }

  private fun bind() {
    mainViewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(MainViewModel::class.java)
    // 订阅render方法根据发送过来的state渲染界面
    disposables += mainViewModel.states().subscribe(this::render)
    // 传递UI的intents给ViewModel
    mainViewModel.processIntents(intents())
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    val menuItem: MenuItem? = menu?.findItem(R.id.menu_summary)
    val isMenuEnabled = accountingDetailController.accountingDetailList.isNotEmpty()
    if ((menuItem?.isEnabled!! && !isMenuEnabled) || (!menuItem.isEnabled && isMenuEnabled)) {
      val resIcon: Drawable? = resources?.getDrawable(R.drawable.ic_show_chart_black_24dp, theme)
      if (!isMenuEnabled)
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

  override fun intents(): Observable<MainIntent> {
    return Observable.merge(initialIntent(), loadNextPageIntent(), deleteAccountingIntent())
  }

  private fun initialIntent(): Observable<MainIntent> = Observable.just(MainIntent.InitialIntent())

  private fun loadNextPageIntent(): Observable<MainIntent> {
    return RxRecyclerView.scrollStateChanges(rv_main_detail)
        .filter { !accountingDetailController.isNoMoreData }
        .filter { !accountingDetailController.isLoadingMore }
        .filter { it == RecyclerView.SCROLL_STATE_IDLE }
        .filter { layoutManager.findLastCompletelyVisibleItemPosition() ==
            accountingDetailController.adapter.itemCount - 1 }
        .map {
          accountingDetailController.accountingDetailList.lastOrNull()?.let {
            MainIntent.LoadNextPageIntent((it as MainAccountingDetailContent).createTime)
          }
        }
  }

  private fun deleteAccountingIntent(): Observable<MainIntent> =
      deleteItemPublisher.map {
        MainIntent.DeleteAccountingIntent(it)
      }

  override fun render(state: MainViewState) {
    accountingDetailController.setData(state.accountingDetailList, state.isLoading)
    accountingDetailController.isNoMoreData = state.isNoMoreData

    if (state.error != null) {
      toast(state.error.message.toString())
    }

    if (state.isNoData) {
      if (tv_main_accounting_no_data.visibility == View.GONE) {
        tv_main_accounting_no_data.visibility = View.VISIBLE
      }
    } else {
      if (tv_main_accounting_no_data.visibility == View.VISIBLE) {
        tv_main_accounting_no_data.visibility = View.GONE
      }
      invalidateOptionsMenu()
    }

  }

  override fun onDestroy() {
    super.onDestroy()

    disposables.dispose()
  }
}
