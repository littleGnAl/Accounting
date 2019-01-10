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
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.success
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    addOrEditMvRxViewModel.loadAccounting(accountingArgs.id)
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
            preState.success(preState.accountingDetailList) { preList ->
              mainMvRxViewModel.addOrEditAccounting(
                  preList,
                  accountingArgs.id,
                  et_add_or_edit_pay_value.text.toString().toFloat(),
                  fbl_tag_container.getCheckedTagName(),
                  showDate,
                  et_add_or_edit_remarks.text.toString()
              )
            }
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
    context?.apply {
      DatePickerDialog(
          this,
          DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            showDate = "$year/${month + 1}/$dayOfMonth"
            timePicker()
          },
          c.get(Calendar.YEAR),
          c.get(Calendar.MONTH),
          c.get(Calendar.DAY_OF_MONTH)
      ).apply {
        datePicker.maxDate = c.timeInMillis
        show()
      }
    }
  }

  override fun invalidate() {
    withState(addOrEditMvRxViewModel) { state ->
      if ((state.accounting as? Fail)?.error != null) {
        activity?.toast(state.accounting.error.message.toString())
        return@withState
      }

      state.accounting()?.apply {
        if (id == 0) return@withState

        if (amount != 0.0f) {
          val amountString = amount.toString()
          et_add_or_edit_pay_value.setText(amountString)
          et_add_or_edit_pay_value.setSelection(amountString.length)
        }

        if (tagName.isNotEmpty()) {
          fbl_tag_container.selectTag(tagName)
        }

        val dateTime = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(createTime)
        tv_add_or_edit_date_value.text = dateTime
        selectedDateAndTimePublisher.onNext(dateTime)
        showDate = dateTime
        remarks?.apply { et_add_or_edit_remarks.setText(this) }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    disposables.dispose()
  }
}
