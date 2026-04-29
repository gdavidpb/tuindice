package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

object Main {
	sealed class State : ViewState() {
		data object Starting : State()

		data class Content(
			val startDestination: Destination,
			val wizardStartRequested: Boolean = false
		) : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		data object StartUp : Action()
		data object RequestReview : Action()
		data object RequestUpdateCheck : Action()
		class SetLastMainSection(val section: MainSection) : Action()
		data object RequestWizardStart : Action()
	}

	sealed class Effect : ViewEffect() {
		object NavigateToGooglePlayServicesUnavailableDialog : Effect()
		data object TriggerReviewFlow : Effect()
		class TriggerUpdateFlow(val action: UpdateAction) : Effect()
		data object NavigateToWizard : Effect()
	}
}
