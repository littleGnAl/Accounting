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
import com.littlegnal.accounting.base.mvibase.MviView
import com.yunmai.scale.coach.common.extensions.plusAssign
import com.yunmai.scale.coach.common.extensions.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_add_or_edit.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * @author littlegnal
 * @date 2017/8/24
 */
class AddOrEditActivity : BaseActivity(), MviView<AddOrEditIntent, AddOrEditViewState> {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  lateinit var addOrEditViewModel: AddOrEditViewModel

  private lateinit var showDate: String

  private val selectedDateAndTimePublisher: PublishSubject<String> = PublishSubject.create()

  private lateinit var saveOrUpdateConfirmObservable: Observable<Boolean>
  private lateinit var inputPayObservable: Observable<String>

  private lateinit var enableConfirmDisposable: Disposable

  private var accountingId: Int? = null

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_or_edit)
    setToolbarWithBack()

    accountingId = intent?.getIntExtra(ACCOUNTING_ID_KEY, ADD).let { if (it == ADD) null else it }

    title = if (accountingId == ADD) {
      getString(R.string.add_or_edit_add_title)
    } else {
      getString(R.string.add_or_edit_edit_title)
    }

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

    val enableConfirmBtn: Observable<Boolean> = Observable.combineLatest(
        inputPayObservable,
        fbl_tag_container.checkedTagNameObservable(),
        selectedDateAndTimePublisher,
        Function3 { pay, tagName, dateTime ->
          pay.isNotEmpty() && tagName.isNotEmpty() && dateTime.isNotEmpty()
        })
    enableConfirmDisposable =
        enableConfirmBtn
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
              btn_add_or_edit_confirm.isEnabled = it
            }

    bind()
  }

  private fun bind() {
    addOrEditViewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(AddOrEditViewModel::class.java)

    disposables += addOrEditViewModel.states().subscribe(this::render)
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
                et_add_or_edit_remarks.text.toString())
          }

//      Observable.combineLatest(
//          saveOrUpdateConfirmObservable,
//          fbl_tag_container.getCheckedTagName(),
//          selectedDateAndTimePublisher,
//          Function3 { _, tagName, selectedDate ->
//            AddOrEditIntent.CreateOrUpdateIntent(
//                accountingId,
//                et_add_or_edit_pay_value.text.toString().toFloat(),
//                tagName,
//                selectedDate,
//                et_add_or_edit_remarks.text.toString())
//          })

  override fun render(state: AddOrEditViewState) {
    if (state.isNeedFinish) {
      finish()
      return
    }

    if (state.error != null) {
      toast(state.error.message.toString())
      return
    }

    state.amount?.apply { et_add_or_edit_pay_value.setText(this) }
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
        c.get(Calendar.DAY_OF_MONTH))
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
        true).run { show() }
  }

  override fun onDestroy() {
    super.onDestroy()

    enableConfirmDisposable.dispose()
    disposables.dispose()
  }

  companion object {
    val ACCOUNTING_ID_KEY = "ACCOUNTING_ID_KEY"
    val ADD = -1

    fun add(activity: Activity) {
      addOrEdit(activity)
    }

    fun edit(activity: Activity, accountingId: Int) {
      addOrEdit(activity, accountingId)
    }

    private fun addOrEdit(activity: Activity, accountingId: Int = ADD) {
      val intent = Intent(activity, AddOrEditActivity::class.java)
      intent.putExtra(ACCOUNTING_ID_KEY, accountingId)
      activity.startActivity(intent)
    }
  }

}