package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.LocalDate
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

		// What the hub says under the profile (attention, week, term, career). Like [sync] every
		// state carries it: the user's observation rebuilds the content, and the hub, which has its
		// own observation, must not be lost when it does.
		abstract val hub: SummaryHubItem

		data class Idle(
			override val sync: SummarySyncItem = SummarySyncItem(),
			override val hub: SummaryHubItem = SummaryHubItem()
		) : State()

		data class Loading(
			override val isUserRefreshing: Boolean = false,
			override val sync: SummarySyncItem = SummarySyncItem(),
			override val hub: SummaryHubItem = SummaryHubItem()
		) : State(isUserRefreshing = isUserRefreshing)

		data class Content(
			val name: String,
			val careerName: String,
			val grade: Float,
			val profilePictureUrl: String,
			val profilePictureVersion: Int = 0,
			val profilePictureLocalPreview: String? = null,
			val isProfilePictureLoading: Boolean,
			override val isUserRefreshing: Boolean,
			val syncStatusText: UiText = UiText.Empty,
			override val sync: SummarySyncItem = SummarySyncItem(),
			override val hub: SummaryHubItem = SummaryHubItem()
		) : State(isUserRefreshing = isUserRefreshing)

		data class Failed(
			override val isUserRefreshing: Boolean = false,
			override val sync: SummarySyncItem = SummarySyncItem(),
			override val hub: SummaryHubItem = SummaryHubItem()
		) : State(isUserRefreshing = isUserRefreshing)

		/** The same state with [hub] in place of the one it carries. */
		fun withHub(hub: SummaryHubItem): State = when (this) {
			is Idle -> copy(hub = hub)
			is Loading -> copy(hub = hub)
			is Content -> copy(hub = hub)
			is Failed -> copy(hub = hub)
		}
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

		// The screen is back in front: the clock is read again instead of waiting for its next tick.
		data object Resumed : Action()

		// A line of the hub was tapped: it asks to be taken to the screen that owns what it says.
		class OpenTarget(val target: SummaryHubTarget) : Action()
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

		// Take the student to the screen that owns what the tapped line says.
		data object NavigateToSchedule : Effect()
		data object NavigateToRecord : Effect()
		data object NavigateToPensum : Effect()

		// The Evaluations tab, on the week that [weekStart] (a Monday) opens, with [evaluationId] in view.
		class NavigateToEvaluationsWeek(
			val weekStart: LocalDate,
			val evaluationId: String
		) : Effect()

		// The Record tab, on the term of [attemptId], with that attempt in view.
		class NavigateToRecordAttempt(
			val attemptId: String
		) : Effect()

		// The form that adds an evaluation, on the Evaluations tab, and the form that plans a term, on the Record tab.
		data object NavigateToAddEvaluation : Effect()
		data object NavigateToCreateTerm : Effect()

		// The Evaluations tab as it opens by itself. The hub has no line that leads there; the Statistics screen does.
		data object NavigateToEvaluations : Effect()

		// The Statistics screen, pushed over the hub: the section's link and its card ask for it.
		data object NavigateToStatistics : Effect()

		// The sheet that shares a closed term as an image, pushed over the hub on the term of [termKey]: the share button of
		// the last term that is over asks for it.
		class NavigateToShareTerm(
			val termKey: String
		) : Effect()
	}
}
