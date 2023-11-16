package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class SummaryDialog : Dialog {
	class ProfilePictureSettings(
		val showTake: Boolean,
		val showRemove: Boolean
	) : SummaryDialog()

	data object RemoveProfilePictureConfirmation : SummaryDialog()

	data object OutdatedPassword : SummaryDialog()
}