package com.gdavidpb.tuindice.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.onClickOnce
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_confirmation.*

class ConfirmationBottomSheetDialog : BottomSheetDialogFragment() {
	@StringRes
	var titleResource: Int = 0
	var titleText: CharSequence = ""

	@StringRes
	var messageResource: Int = 0
	var messageText: CharSequence = ""

	@StringRes
	private var positiveResource: Int = 0
	private var onPositiveClick: DialogFragment.() -> Unit = {}

	@StringRes
	private var negativeResource: Int = 0
	private var onNegativeClick: DialogFragment.() -> Unit = {}

	fun positiveButton(@StringRes resId: Int, onClick: DialogFragment.() -> Unit = {}) {
		positiveResource = resId
		onPositiveClick = onClick
	}

	fun negativeButton(@StringRes resId: Int, onClick: DialogFragment.() -> Unit = {}) {
		negativeResource = resId
		onNegativeClick = onClick
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.dialog_confirmation, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		when {
			titleResource != 0 -> tViewQuestionTitle.setText(titleResource)
			titleText.isNotEmpty() -> tViewQuestionTitle.text = titleText
			else -> error("'titleResource' or 'titleText' is missed")
		}

		when {
			messageResource != 0 -> tViewQuestionMessage.setText(messageResource)
			messageText.isNotEmpty() -> tViewQuestionMessage.text = messageText
			else -> error("'messageResource' or 'messageText' is missed")
		}

		if (positiveResource != 0)
			btnPositive.setText(positiveResource)
		else
			btnPositive.isGone = true

		if (negativeResource != 0)
			btnNegative.setText(negativeResource)
		else
			btnNegative.isGone = true

		btnPositive.onClickOnce { onPositiveClick(); dismiss() }
		btnNegative.onClickOnce { onNegativeClick(); dismiss() }
	}
}