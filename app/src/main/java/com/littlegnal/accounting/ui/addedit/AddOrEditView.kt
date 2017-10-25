package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvi.MviView
import io.reactivex.Observable

/**
 * @author littlegnal
 * @date 2017/8/24
 */
interface AddOrEditView : MviView<AddOrEditViewState> {
  fun loadDataIntent(): Observable<Int>

  fun saveOrUpdateIntent(): Observable<Array<String>>
}