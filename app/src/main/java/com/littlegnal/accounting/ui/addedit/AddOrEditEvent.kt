package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.db.Accounting

/**
 * [com.littlegnal.accounting.base.eventbus.RxBus]通知事件
 * @author littlegnal
 * @date 2017/12/19
 */
data class AddOrEditEvent(val accounting: Accounting)