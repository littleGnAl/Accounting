package com.littlegnal.accounting.ui.addedit

import android.os.Parcelable
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.littlegnal.accounting.db.Accounting
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddOrEditMvRxStateArgs(val id: Int = -1) : Parcelable

data class AddOrEditMvRxViewState(
  val id: Int = -1,
  val amount: String? = null,
  val tagName: String? = null,
  val dateTime: String? = null,
  val remarks: String? = null,
  val accounting: Async<Accounting> = Uninitialized
) : MvRxState {
  constructor(args: AddOrEditMvRxStateArgs): this(id = args.id)
}
