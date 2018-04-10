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

package com.littlegnal.accounting.ui.addedit

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.BaseActivity
import com.littlegnal.accounting.base.mvi.MviIntent
import com.littlegnal.accounting.base.mvi.MviView
import com.littlegnal.accounting.base.mvi.MviViewModel
import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_add_or_edit.btn_add_or_edit_confirm
import kotlinx.android.synthetic.main.activity_add_or_edit.et_add_or_edit_pay_value
import kotlinx.android.synthetic.main.activity_add_or_edit.et_add_or_edit_remarks
import kotlinx.android.synthetic.main.activity_add_or_edit.fbl_tag_container
import kotlinx.android.synthetic.main.activity_add_or_edit.sv_add_or_edit
import kotlinx.android.synthetic.main.activity_add_or_edit.tv_add_or_edit_date_value
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 增加或修改页面
 */
class AddOrEditActivity : BaseActivity(), MviView<AddOrEditIntent, AddOrEditViewState> {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  lateinit var addOrEditViewModel: AddOrEditViewModel

  private lateinit var showDate: String

  private val selectedDateAndTimePublisher: PublishSubject<String> = PublishSubject.create()

  private lateinit var saveOrUpdateConfirmObservable: Observable<Boolean>
  private lateinit var inputPayObservable: Observable<String>

  private var accountingId: Int? = null

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_or_edit)
    setToolbarWithBack()

    accountingId = intent?.getIntExtra(ACCOUNTING_ID_KEY, -1)
        .let {
          if (it == -1) null else it
        }

    title = accountingId?.let { getString(R.string.add_or_edit_edit_title) }
        ?: getString(R.string.add_or_edit_add_title)

    tv_add_or_edit_date_value.setOnClickListener {
      hideSoftKeyboard()
      datePicker()
    }

    sv_add_or_edit.setOnTouchListener { _, _ ->
      hideSoftKeyboard()
      return@setOnTouchListener false
    }

    saveOrUpdateConfirmObservable = RxView.clicks(btn_add_or_edit_confirm)
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map { true }

    inputPayObservable = RxTextView.textChanges(et_add_or_edit_pay_value)
        .map { it.toString() }
        .share()

    // 只有输入金额，选择tag，选择时间后才允许点击确认按钮
    val enableConfirmBtn: Observable<Boolean> = Observable.combineLatest(
        inputPayObservable,
        fbl_tag_container.checkedTagNameObservable(),
        selectedDateAndTimePublisher,
        Function3 { pay, tagName, dateTime ->
          pay.isNotEmpty() && tagName.isNotEmpty() && dateTime.isNotEmpty()
        })
    disposables += enableConfirmBtn
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          btn_add_or_edit_confirm.isEnabled = it
        }

    bind()
  }

  /**
   * 连接[MviView]和[MviViewModel]，需要在传送[MviIntent]s之前让[MviViewModel]订阅[MviView.render]方法，
   * 否则会丢失[MviViewState]s
   */
  private fun bind() {
    addOrEditViewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(AddOrEditViewModel::class.java)

    // 订阅render方法根据发送过来的state渲染界面
    disposables += addOrEditViewModel.states()
        .subscribe(this::render)
    // 传递UI的intents给ViewModel
    addOrEditViewModel.processIntents(intents())
  }

  override fun intents(): Observable<AddOrEditIntent> =
    Observable.merge(initialIntent(), createOrUpdateIntent())

  private fun initialIntent(): Observable<AddOrEditIntent> {
    return Observable.just(AddOrEditIntent.InitialIntent(accountingId))
  }

  private fun createOrUpdateIntent(): Observable<AddOrEditIntent> =
    RxView.clicks(btn_add_or_edit_confirm)
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map {
          AddOrEditIntent.CreateOrUpdateIntent(
              accountingId,
              et_add_or_edit_pay_value.text.toString().toFloat(),
              fbl_tag_container.getCheckedTagName(),
              showDate,
              et_add_or_edit_remarks.text.toString()
          )
        }

  override fun render(state: AddOrEditViewState) {
    if (state.isNeedFinish) {
      finish()
      return
    }

    if (state.error != null) {
      toast(state.error.message.toString())
      return
    }

    state.amount?.apply {
      et_add_or_edit_pay_value.setText(this)
      et_add_or_edit_pay_value.setSelection(this.length)
    }
    state.tagName?.apply { fbl_tag_container.selectTag(this) }
    state.dateTime?.apply {
      tv_add_or_edit_date_value.text = this
      selectedDateAndTimePublisher.onNext(this)
      showDate = this
    }
    state.remarks?.apply { et_add_or_edit_remarks.setText(this) }
  }

  private fun datePicker() {
    val c = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        this,
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
          showDate = "$year/${month + 1}/$dayOfMonth"
          timePicker()
        },
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = c.timeInMillis
    datePickerDialog.show()
  }

  private fun timePicker() {
    val c = Calendar.getInstance()
    TimePickerDialog(
        this,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
          showDate += " $hourOfDay:$minute"
          tv_add_or_edit_date_value.text = showDate
          selectedDateAndTimePublisher.onNext(showDate)
        },
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.MINUTE),
        true
    ).run { show() }
  }

  override fun onDestroy() {
    super.onDestroy()

    disposables.dispose()
  }

  companion object {
    val ACCOUNTING_ID_KEY = "ACCOUNTING_ID_KEY"

    fun add(activity: Activity) {
      addOrEdit(activity)
    }

    fun edit(
      activity: Activity,
      accountingId: Int
    ) {
      addOrEdit(activity, accountingId)
    }

    private fun addOrEdit(
      activity: Activity,
      accountingId: Int = -1
    ) {
      val intent = Intent(activity, AddOrEditActivity::class.java)
      intent.putExtra(ACCOUNTING_ID_KEY, accountingId)
      activity.startActivity(intent)
    }
  }
}