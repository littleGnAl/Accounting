package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvibase.MviViewState


/**
 * @author littlegnal
 * @date 2017/8/24
 */
data class AddOrEditViewState(
    val isLoading: Boolean,
    val error: Throwable?,
    val amount: String? = null,
    val tagName: String? = null,
    val dateTime: String? = null,
    val remarks: String? = null,
    val isNeedFinish: Boolean = false
) : MviViewState {
  companion object {
    fun idle() = AddOrEditViewState(false, null)
  }
}