package com.littlegnal.accounting.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.littlegnal.accounting.R

/**
 * @author littlegnal
 * @date 2017/10/13
 */
class DeleteConfirmDialog : DialogFragment() {

  var okClickListener: DialogInterface.OnClickListener? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(activity, R.style.DeleteDialogStyle)
        .setMessage(R.string.delete_confirm_tips)
        .setPositiveButton(android.R.string.ok, okClickListener)
        .setNegativeButton(android.R.string.cancel) {
          dialogInterface, _ -> dialogInterface.dismiss()
        }
        .create()
  }
}