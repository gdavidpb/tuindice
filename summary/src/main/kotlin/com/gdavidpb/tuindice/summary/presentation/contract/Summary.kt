package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Summary {
	sealed class State : ViewState {
		data object Loading : State()

		data class Content(
			val name: String,
			val lastUpdate: String,
			val careerName: String,
			val grade: Float,
			val enrolledSubjects: Int,
			val enrolledCredits: Int,
			val approvedSubjects: Int,
			val approvedCredits: Int,
			val retiredSubjects: Int,
			val retiredCredits: Int,
			val failedSubjects: Int,
			val failedCredits: Int,
			val profilePictureUrl: String,
			val isGradeVisible: Boolean,
			val isProfilePictureLoading: Boolean,
			val isLoading: Boolean,
			val isUpdated: Boolean,
			val isUpdating: Boolean
		) : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction {
		data object LoadSummary : Action()
		data object TakeProfilePicture : Action()
		data object PickProfilePicture : Action()
		class UploadProfilePicture(val path: String) : Action()
		data object OpenProfilePictureSettings : Action()
		data object RemoveProfilePicture : Action()
		data object ConfirmRemoveProfilePicture : Action()
		data object CloseDialog : Action()
	}

	sealed class Effect : ViewEffect {
		class OpenCamera(val output: String) : Effect()
		data object OpenPicker : Effect()
		data object NavigateToOutdatedPassword : Effect()
		class ShowSnackBar(val message: String) : Effect()
		class ShowProfilePictureSettingsDialog(val showRemove: Boolean) : Effect()
		data object ShowRemoveProfilePictureConfirmationDialog : Effect()
		data object CloseDialog : Effect()
	}
}