package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.*

class UpdatePasswordBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_update_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnCancel.onClickOnce(::onCancelClick)
    }

    override fun isCancelable(): Boolean {
        return false
    }

    private fun onCancelClick() {
        dismiss()
    }
}