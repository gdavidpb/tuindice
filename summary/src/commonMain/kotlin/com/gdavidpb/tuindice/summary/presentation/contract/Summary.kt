package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.summary.presentation.resource.summaryScreenTitle

object Summary {
	sealed class State(
		override val topBarTitle: String = summaryScreenTitle(),
		override val topBarConfig: TopBarConfig = TopBarConfig.Summary,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
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

	sealed class Action : ViewAction() {
		data object LoadSummary : Action()
		data object TakeProfilePicture : Action()
		data object PickProfilePicture : Action()
		class UploadProfilePicture(val uri: PlatformUri) : Action()
		data object OpenProfilePictureSettings : Action()
		data object RemoveProfilePicture : Action()
		data object ConfirmRemoveProfilePicture : Action()
	}

	sealed class Effect : ViewEffect() {
		class NavigateToProfilePictureSettingsDialog(val showRemove: Boolean) : Effect()
		data object NavigateToOutdatedPassword : Effect()
		data object NavigateToRemoveProfilePictureConfirmationDialog : Effect()
		class OpenCamera(val output: PlatformFileRef) : Effect()
		data object OpenPicker : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}
