package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem
import io.github.vinceglb.filekit.PlatformFile
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.top_bar_summary

object Summary {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_summary),
		override val topBarConfig: TopBarConfig = TopBarConfig.Summary,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true,
		open val isUserRefreshing: Boolean = false
	) : ViewState {
		// What the sync says of the account. Every state carries it: the failed screen tells a new
		// student apart by it, the content shows it, and the states in between must not lose what
		// was last observed, because the observation only speaks again when something changes.
		abstract val sync: SummarySyncItem

		data class Idle(
			override val sync: SummarySyncItem = SummarySyncItem()
		) : State()

		data class Loading(
			override val isUserRefreshing: Boolean = false,
			override val sync: SummarySyncItem = SummarySyncItem()
		) : State(isUserRefreshing = isUserRefreshing)

		data class Content(
			val name: String,
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
			val profilePictureVersion: Int = 0,
			val profilePictureLocalPreview: String? = null,
			val isProfilePictureLoading: Boolean,
			override val isUserRefreshing: Boolean,
			val syncStatusText: UiText = UiText.Empty,
			override val sync: SummarySyncItem = SummarySyncItem()
		) : State(isUserRefreshing = isUserRefreshing)

		data class Failed(
			override val isUserRefreshing: Boolean = false,
			override val sync: SummarySyncItem = SummarySyncItem()
		) : State(isUserRefreshing = isUserRefreshing)
	}

	sealed class Action : ViewAction {
		data object ObserveSummary : Action()
		data object RefreshSummary : Action()
		data object TakeProfilePicture : Action()
		data object PickProfilePicture : Action()
		class UploadProfilePicture(val file: PlatformFile) : Action()
		data object OpenProfilePictureSettings : Action()
		data object RemoveProfilePicture : Action()
		data object ConfirmRemoveProfilePicture : Action()
	}

	sealed class Effect : ViewEffect {
		class ShowProfilePictureSettingsDialog(
			val showRemove: Boolean,
			val isCameraAvailable: Boolean
		) : Effect()
		data object ShowRemoveProfilePictureConfirmationDialog : Effect()
		data object OpenCamera : Effect()
		data object OpenPicker : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}
