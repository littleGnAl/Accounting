package com.littlegnal.accounting.ui.addedit

import com.littlegnal.accounting.base.mvibase.MviIntent

/**
 * @author littlegnal
 * @date 2017/12/16
 */
sealed class AddOrEditIntent : MviIntent {

  data class InitialIntent(val accountingId: Int?) : AddOrEditIntent()

  data class CreateOrUpdateIntent(
      val accountingId: Int?,
      val amount: Float,
      val tagName: String,
      val showDate: String,
      val remarks: String?
  ) : AddOrEditIntent()
}