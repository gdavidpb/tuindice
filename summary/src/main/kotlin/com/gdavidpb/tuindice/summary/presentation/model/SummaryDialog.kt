package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class SummaryDialog : Dialog {
	class ProfilePictureSettings(
		val showTake: Boolean,
		val showRemove: Boolean
	) : SummaryDialog()

	object SignOutConfirmation : SummaryDialog()

	object RemoveProfilePictureConfirmation : SummaryDialog()

	object OutdatedPassword : SummaryDialog()
}