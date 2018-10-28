package com.littlegnal.accounting.ui.addedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.main.MainActivity
import com.littlegnal.accounting.ui.main.MainViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_add_or_edit.btn_add_or_edit_confirm
import kotlinx.android.synthetic.main.fragment_add_or_edit.et_add_or_edit_pay_value
import kotlinx.android.synthetic.main.fragment_add_or_edit.et_add_or_edit_remarks
import kotlinx.android.synthetic.main.fragment_add_or_edit.fbl_tag_container
import kotlinx.android.synthetic.main.fragment_add_or_edit.sv_add_or_edit
import kotlinx.android.synthetic.main.fragment_add_or_edit.tv_add_or_edit_date_value
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AddOrEditFragment : BaseMvRxFragment() {

  private lateinit var showDate: String

  private val selectedDateAndTimePublisher: PublishSubject<String> = PublishSubject.create()

  private lateinit var saveOrUpdateConfirmObservable: Observable<Boolean>
  private lateinit var inputPayObservable: Observable<String>

  private val accountingArgs: AddOrEditMvRxStateArgs by args()

  private val disposables = CompositeDisposable()

  private val mainMvRxViewModel by activityViewModel(MainViewModel::class)

  private val addOrEditMvRxViewModel by fragmentViewModel(AddOrEditViewModel::class)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val title = if (accountingArgs.id != -1) {
      getString(R.string.add_or_edit_edit_title)
    } else {
      getString(R.string.add_or_edit_add_title)
    }
    (activity as MainActivity).updateTitle(title)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_add_or_edit, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    tv_add_or_edit_date_value.setOnClickListener {
      hideSoftKeyboard()
      datePicker()
    }

    sv_add_or_edit.setOnTouchListener { _, _ ->
      hideSoftKeyboard()
      return@setOnTouchListener false
    }

    saveOrUpdateConfirmObservable = btn_add_or_edit_confirm.clicks()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map { true }

    inputPayObservable = et_add_or_edit_pay_value.textChanges()
        .map { it.toString() }
        .share()

    // 只有输入金额，选择tag，选择时间后才允许点击确认按钮
    disposables += Observable.combineLatest(
        inputPayObservable,
        fbl_tag_container.checkedTagNameObservable(),
        selectedDateAndTimePublisher,
        Function3<String, String, String, Boolean> { pay, tagName, dateTime ->
          pay.isNotEmpty() && tagName.isNotEmpty() && dateTime.isNotEmpty()
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          btn_add_or_edit_confirm.isEnabled = it
        }

    disposables += btn_add_or_edit_confirm.clicks()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .subscribe {
          withState(mainMvRxViewModel) { preState ->
            mainMvRxViewModel.addOrEditAccounting(
                preState.accountingDetailList,
                accountingArgs.id,
                et_add_or_edit_pay_value.text.toString().toFloat(),
                fbl_tag_container.getCheckedTagName(),
                showDate,
                et_add_or_edit_remarks.text.toString()
            )
          }

          findNavController().navigateUp()
        }
  }

  private var inputMethodManager: InputMethodManager? = null

  private fun hideSoftKeyboard() {
    if (inputMethodManager == null) {
      inputMethodManager = context?.getSystemService(
          Context.INPUT_METHOD_SERVICE
      ) as InputMethodManager
    }
    if (activity?.window?.attributes?.softInputMode !=
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      if (activity?.currentFocus != null) {
        inputMethodManager?.hideSoftInputFromWindow(
            activity?.currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
      }
    }
  }

  private fun timePicker() {
    val c = Calendar.getInstance()
    TimePickerDialog(
        context,
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

  private fun datePicker() {
    val c = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
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

  override fun invalidate() {
    withState(addOrEditMvRxViewModel) { state ->
      if (state.error != null) {
        activity?.toast(state.error.message.toString())
        return@withState
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
  }

  override fun onDestroyView() {
    super.onDestroyView()

    disposables.dispose()
  }
}
