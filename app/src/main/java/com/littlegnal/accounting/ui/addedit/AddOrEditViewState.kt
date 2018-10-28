package com.littlegnal.accounting.ui.addedit

import android.os.Parcelable
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddOrEditMvRxStateArgs(val id: Int = -1) : Parcelable

data class AddOrEditMvRxViewState(
  val id: Int = -1,
  val isLoading: Boolean = false,
  val error: Throwable? = null,
  @PersistState val amount: String? = null,
  @PersistState val tagName: String? = null,
  @PersistState val dateTime: String? = null,
  @PersistState val remarks: String? = null
) : MvRxState {
  constructor(args: AddOrEditMvRxStateArgs): this(id = args.id)
}
