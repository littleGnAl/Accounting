package com.littlegnal.accounting.ui.addedit

/**
 * @author littlegnal
 * @date 2017/12/16
 */
sealed class AddOrEditAction {

  class SkipAction : AddOrEditAction()

  data class LoadAccountingAction(val accountingId: Int) : AddOrEditAction()

  data class CreateAccountingAction(
      val amount: Float,
      val tagName: String,
      val showDate: String,
      val remarks: String?
  ) : AddOrEditAction()

  data class UpdateAccountingAction(
      val accountingId: Int,
      val amount: Float,
      val tagName: String,
      val showDate: String,
      val remarks: String?
  ) : AddOrEditAction()

//  data class CreateOrUpdateAction(
//      val accountingId: Int,
//      val amount: Float,
//      val tagName: String,
//      val showDate: String,
//      val remarks: String?
//  ) : AddOrEditAction()
}