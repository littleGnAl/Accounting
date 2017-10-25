package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvi.MviViewState

/**
 * @author littlegnal
 * @date 2017/8/24
 */
data class AddOrEditViewState(
    val amount: String? = null,
    val tagName: String? = null,
    val dateTime: String? = null,
    val remarks: String? = null) : MviViewState