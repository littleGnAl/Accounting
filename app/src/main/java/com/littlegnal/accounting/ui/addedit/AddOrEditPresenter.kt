package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import android.support.annotation.VisibleForTesting
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.littlegnal.accounting.base.mvi.MviView
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * @author littlegnal
 * @date 2017/8/24
 */
class AddOrEditPresenter constructor(
    private var accountingDao: AccountingDao,
    private var addOrUpdateObservable: PublishSubject<Accounting>,
    private val addOrEditNavigator: AddOrEditNavigator) {

  private lateinit var saveOrUpdateDisposable: Disposable

  @SuppressLint("SimpleDateFormat")
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

  @VisibleForTesting
  val NOW = Calendar.getInstance().time

  fun bindIntents() {
//    val loadDataIntent: Observable<AddOrEditViewState> =
//      intent { it.loadDataIntent() }
//        .flatMap {
//          if (it == AddOrEditActivity.ADD) {
//            Observable.just(AddOrEditViewState(
//                dateTime = dateTimeFormat.format(NOW.time)))
//                .subscribeOn(Schedulers.io())
//          } else {
//            accountingDao.getAccountingById(it)
//                .map {
//                  AddOrEditViewState(
//                    it.amount.toString(),
//                    it.tagName,
//                    dateTimeFormat.format(it.createTime),
//                    it.remarks) }
//                .toObservable()
//                .subscribeOn(Schedulers.io())
//          }
//        }
//        .observeOn(AndroidSchedulers.mainThread())
//
//    saveOrUpdateDisposable =
//        intent { it.saveOrUpdateIntent() }
//        .doOnNext { Timber.d("saveOrUpdateIntent") }
//        .flatMap {
//          val id: Int = it[0].toInt()
//          val amount: Float = it[1].toFloat()
//          val tagName: String = it[2]
//          val dateTime: Date = dateTimeFormat.parse(it[3])
//          val remarks: String? = it[4]
//          val accounting = Accounting(amount, dateTime, tagName, remarks)
//          if (id != AddOrEditActivity.ADD) {
//            accounting.id = id
//          }
//          Observable.just(accounting)
//              .doOnNext {
//                val insertedId = accountingDao.insertAccounting(it).toInt()
//                if (id == AddOrEditActivity.ADD) {
//                  it.id = insertedId
//                }
//              }
//              .subscribeOn(Schedulers.io())
//        }
//        .doOnNext { addOrUpdateObservable.onNext(it) }
//        .doOnNext { addOrEditNavigator.finish() }
//        .subscribe()
//
//    subscribeViewState(loadDataIntent, AddOrEditView::render)
  }

  fun unbindIntents() {
    saveOrUpdateDisposable.dispose()
  }
}