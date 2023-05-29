package com.gdavidpb.tuindice.summary.presentation.model

sealed class SummaryDialog {
	class ProfilePictureSettings(
		val showPick: Boolean,
		val showTake: Boolean,
		val showRemove: Boolean
	) : SummaryDialog()

	object RemoveProfilePictureConfirmation : SummaryDialog()

	object OutdatedPassword : SummaryDialog()
}