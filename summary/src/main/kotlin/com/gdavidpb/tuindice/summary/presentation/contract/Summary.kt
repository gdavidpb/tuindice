package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object Summary {
	sealed class State : ViewState {
		object Loading : State()

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

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadSummary : Action()
		object TakeProfilePicture : Action()
		object PickProfilePicture : Action()
		class UploadProfilePicture(val path: String) : Action()
		object OpenProfilePictureSettings : Action()
		object RemoveProfilePicture : Action()
		object ConfirmRemoveProfilePicture : Action()
		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		class OpenCamera(val output: String) : Event()
		object OpenPicker : Event()
		object NavigateToOutdatedPassword : Event()
		class ShowSnackBar(val message: String) : Event()
		class ShowProfilePictureSettingsDialog(val showRemove: Boolean) : Event()
		object ShowRemoveProfilePictureConfirmationDialog : Event()
		object CloseDialog : Event()
	}
}