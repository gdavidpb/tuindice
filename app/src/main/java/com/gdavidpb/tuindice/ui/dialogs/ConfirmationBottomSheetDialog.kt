package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_confirmation.*

class ConfirmationBottomSheetDialog(
        @StringRes private val titleResource: Int = 0,
        private val titleText: CharSequence = "",
        @StringRes private val messageResource: Int = 0,
        private val messageText: CharSequence = "",
        @StringRes private val positiveResource: Int = 0,
        @StringRes private val negativeResource: Int = 0,
        private val positiveOnClick: DialogFragment.() -> Unit = {},
        private val negativeOnClick: DialogFragment.() -> Unit = {}
) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when {
            titleResource != 0 -> tViewQuestionTitle.setText(titleResource)
            titleText.isNotEmpty() -> tViewQuestionTitle.text = titleText
        }

        when {
            messageResource != 0 -> tViewQuestionMessage.setText(messageResource)
            messageText.isNotEmpty() -> tViewQuestionMessage.text = messageText
        }

        if (positiveResource != 0)
            btnPositive.setText(positiveResource)
        else
            btnPositive.isGone = true

        if (negativeResource != 0)
            btnNegative.setText(negativeResource)
        else
            btnNegative.isGone = true

        btnPositive.onClickOnce { positiveOnClick(); dismiss() }
        btnNegative.onClickOnce { negativeOnClick(); dismiss() }
    }
}