package com.littlegnal.accounting.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import timber.log.Timber

abstract class BaseFragment : BaseMvRxFragment() {

  protected val epoxyController by lazy { epoxyController() }

  /**
   * Provide the EpoxyController to use when building models for this Fragment.
   * Basic usages can simply use [simpleController]
   */
  abstract fun epoxyController(): MvRxEpoxyController

  override fun onDestroyView() {
    epoxyController.cancelPendingModelBuild()
    super.onDestroyView()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    Timber.d("onCreateView")
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    Timber.d("OnViewCreated")
    super.onViewCreated(view, savedInstanceState)
  }
}
