package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

object Main {
	sealed class State : ViewState {
		// The way to the sign-in owed to the user, in every state: a transition never drops it, only
		// the interface attending it does (Action.SessionEndHandled).
		abstract val pendingSessionEnd: PendingSessionEnd?

		data class Starting(
			override val pendingSessionEnd: PendingSessionEnd? = null
		) : State()

		data class AppUnavailable(
			val notice: AppAvailabilityNotice,
			override val pendingSessionEnd: PendingSessionEnd? = null
		) : State()

		data class OutdatedApp(
			val outdatedAppState: OutdatedAppState,
			override val pendingSessionEnd: PendingSessionEnd? = null
		) : State()

		// syncStatus: how the last sync ended, followed while the content is up. The host asks
		// for the password again when it requires one, unless the user has already closed that
		// dialog for this very problem (isUpdatePasswordDismissed).
		data class Content(
			val startDestination: Destination,
			val syncStatus: SyncStatus = SyncStatus.Healthy,
			val isUpdatePasswordDismissed: Boolean = false,
			override val pendingSessionEnd: PendingSessionEnd? = null
		) : State()

		data class Failed(
			override val pendingSessionEnd: PendingSessionEnd? = null
		) : State()
	}

	sealed class Action : ViewAction {
		data object StartUp : Action()
		class ShowOutdatedApp(val outdatedAppState: OutdatedAppState) : Action()
		data object RequestReview : Action()
		data object RequestUpdateCheck : Action()
		data object ClickUpdateApp : Action()
		class UpdateFlowCompleted(val result: UpdateLaunchResult) : Action()
		data object RequestSync : Action()
		data object NoteSyncUnavailable : Action()
		data object NoteSyncFailed : Action()
		class SetLastMainSection(val section: MainSection) : Action()
		data object DismissUpdatePassword : Action()
		data object RequestSignOut : Action()

		// The interface attended the pending end of the session [id]: it took the user to the sign-in and
		// showed the notice, if there was one. A stale id (a newer end is pending) changes nothing.
		class SessionEndHandled(val id: Long) : Action()
	}

	sealed class Effect : ViewEffect {
		object NavigateToGooglePlayServicesUnavailableDialog : Effect()
		data object TriggerReviewFlow : Effect()
		class TriggerUpdateFlow(val action: UpdateAction) : Effect()
		class OpenUpdateStoreFallback(
			val result: UpdateLaunchResult.OpenStoreFallback
		) : Effect()

		class ShowSnackBar(val message: String) : Effect()
		class NavigateToSignOutDialog(val pendingChanges: PendingChanges) : Effect()
	}
}
