package com.littlegnal.accounting.ui.main.adapter

import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/10
 */
data class MainAccountingDetailContent(
    val id: Int,
    val amount: String,
    val tagName: String,
    val remarks: String?,
    val time: String,
    val createTime: Date) : MainAccountingDetail