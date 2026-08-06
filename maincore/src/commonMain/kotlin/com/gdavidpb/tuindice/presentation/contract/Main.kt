package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
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

		data class Content(
			val startDestination: Destination
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
	}

	sealed class Effect : ViewEffect {
		object NavigateToGooglePlayServicesUnavailableDialog : Effect()
		data object TriggerReviewFlow : Effect()
		class TriggerUpdateFlow(val action: UpdateAction) : Effect()
		class OpenUpdateStoreFallback(
			val result: UpdateLaunchResult.OpenStoreFallback
		) : Effect()

		class ShowSnackBar(val message: String) : Effect()
	}
}
