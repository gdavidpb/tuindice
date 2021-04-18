package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_confirmation.*

class ConfirmationBottomSheetDialog(
        @StringRes private val titleResource: Int,
        @StringRes private val messageResource: Int,
        @StringRes private val positiveResource: Int,
        @StringRes private val negativeResource: Int,
        private val positiveOnClick: DialogFragment.() -> Unit = {},
        private val negativeOnClick: DialogFragment.() -> Unit = {}
) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tViewQuestionTitle.setText(titleResource)
        tViewQuestionMessage.setText(messageResource)

        btnPositive.setText(positiveResource)
        btnNegative.setText(negativeResource)

        btnPositive.onClickOnce { positiveOnClick(); dismiss() }
        btnNegative.onClickOnce { negativeOnClick(); dismiss() }
    }
}