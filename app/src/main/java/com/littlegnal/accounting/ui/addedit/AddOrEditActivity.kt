package com.littlegnal.accounting.ui.addedit

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.mvi.BaseMviActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
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
class AddOrEditActivity :
    BaseMviActivity<AddOrEditView, AddOrEditPresenter>(), AddOrEditView, AddOrEditNavigator {

  @Inject
  lateinit var addOrEditPresenter: AddOrEditPresenter

  private lateinit var showDate: String

  private val selectedDateAndTimePublisher: PublishSubject<String> = PublishSubject.create()

  private lateinit var saveOrUpdateConfirmObservable: Observable<Boolean>
  private lateinit var inputPayObservable: Observable<String>

  private lateinit var enableConfirmDisposable: Disposable

  private var accountingId: Int? = ADD

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_or_edit)
    setToolbarWithBack()

    accountingId = intent?.getIntExtra(ACCOUNTING_ID_KEY, ADD)

    title = if (accountingId == ADD) {
      getString(R.string.add_or_edit_add_title)
    } else {
      getString(R.string.add_or_edit_edit_title)
    }

    tv_add_or_edit_date_value.setOnClickListener {
      datePicker()
    }

    sv_add_or_edit.setOnTouchListener { _, _ ->
      hideSoftKeyboard()
      return@setOnTouchListener false
    }

    saveOrUpdateConfirmObservable = RxView.clicks(btn_add_or_edit_confirm)
        .share()
        .map { true }

    inputPayObservable = RxTextView.textChanges(et_add_or_edit_pay_value)
        .map { it.toString() }
        .share()

    val enableConfirmBtn: Observable<Boolean> = Observable.combineLatest(
        inputPayObservable,
        fbl_tag_container.getCheckedTagName(),
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
  }

  override fun createPresenter(): AddOrEditPresenter {
    return addOrEditPresenter
  }

  override fun loadDataIntent(): Observable<Int> {
    return Observable.just(accountingId)
  }

  override fun saveOrUpdateIntent(): Observable<Array<String>> {
    return Observable.combineLatest(
        saveOrUpdateConfirmObservable.throttleFirst(300, TimeUnit.MILLISECONDS),
        fbl_tag_container.getCheckedTagName(),
        BiFunction { _, tagName ->
          arrayOf(
              accountingId.toString(),
              et_add_or_edit_pay_value.text.toString(),
              tagName,
              showDate,
              et_add_or_edit_remarks.text.toString())
        })
  }

  override fun render(viewState: AddOrEditViewState) {
    renderDataState(viewState)
  }

  private fun renderDataState(vs: AddOrEditViewState) {
    vs.amount?.apply { et_add_or_edit_pay_value.setText(this) }
    vs.tagName?.apply { fbl_tag_container.selectTag(this) }
    vs.dateTime?.apply {
      tv_add_or_edit_date_value.text = this
      selectedDateAndTimePublisher.onNext(this)
      showDate = this
    }
    vs.remarks?.apply { et_add_or_edit_remarks.setText(this) }
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
    val timePickerDialog = TimePickerDialog(
        this,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
          showDate += " $hourOfDay:$minute"
          tv_add_or_edit_date_value.text = showDate
          selectedDateAndTimePublisher.onNext(showDate)
        },
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.MINUTE),
        true)
    timePickerDialog.show()
  }

  override fun onDestroy() {
    super.onDestroy()

    enableConfirmDisposable.dispose()
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