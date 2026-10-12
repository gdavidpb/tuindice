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
		data object Starting : State()

		data class AppUnavailable(
			val notice: AppAvailabilityNotice
		) : State()

		data class OutdatedApp(
			val outdatedAppState: OutdatedAppState
		) : State()

		// syncStatus: how the last sync ended, followed while the content is up. The host asks
		// for the password again when it requires one, unless the user has already closed that
		// dialog for this very problem (isUpdatePasswordDismissed).
		data class Content(
			val startDestination: Destination,
			val syncStatus: SyncStatus = SyncStatus.Healthy,
			val isUpdatePasswordDismissed: Boolean = false
		) : State()

		data object Failed : State()
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

		// The interface showed the notice of a session that ended behind the user's back.
		data object SessionInvalidationShown : Action()

		// The interface could not show the notice (the route was cancelled before it could): it stays
		// pending, and the next one may be told.
		data object SessionInvalidationNotShown : Action()
	}

	sealed class Effect : ViewEffect {
		object NavigateToGooglePlayServicesUnavailableDialog : Effect()
		data object TriggerReviewFlow : Effect()
		class TriggerUpdateFlow(val action: UpdateAction) : Effect()
		class OpenUpdateStoreFallback(
			val result: UpdateLaunchResult.OpenStoreFallback
		) : Effect()

		class ShowSnackBar(val message: String) : Effect()

		// Take the user to the sign-in. The message is the notice that goes with it, or null when there is
		// nothing to tell (the notice is already on its way to the screen, or was shown): the navigation
		// never depends on it.
		class SessionInvalidated(val message: String?) : Effect()
		class NavigateToSignOutDialog(val pendingChanges: PendingChanges) : Effect()
	}
}
