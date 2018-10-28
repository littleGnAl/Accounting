/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import com.littlegnal.accounting.R

/**
 * 确认删除弹窗
 */
class DeleteConfirmDialog : DialogFragment() {

  var okClickListener: DialogInterface.OnClickListener? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return context?.let {
      AlertDialog.Builder(it, R.style.DeleteDialogStyle)
          .setMessage(R.string.delete_confirm_tips)
          .setPositiveButton(android.R.string.ok, okClickListener)
          .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
            dialogInterface.dismiss()
          }
          .create()
    }!!
  }

  override fun onDestroyView() {
    okClickListener = null
    super.onDestroyView()
  }
}
