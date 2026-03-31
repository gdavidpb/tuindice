package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

object Main {
	sealed class State : ViewState() {
		data object Starting : State()

		data class Content(
			val startDestination: Destination,
			override val topBarTitle: String = "",
			override val topBarConfig: TopBarConfig? = null,
			override val isTopBarVisible: Boolean = false,
			override val isBottomBarVisible: Boolean = false
		) : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		class UpdateState(val state: State) : Action()
		data object StartUp : Action()
		data object RequestReview : Action()
		data object RequestUpdateCheck : Action()
		class SetLastMainSection(val section: MainSection) : Action()
	}

	sealed class Effect : ViewEffect() {
		object NavigateToGooglePlayServicesUnavailableDialog : Effect()
		data object TriggerReviewFlow : Effect()
		class TriggerUpdateFlow(val action: UpdateAction) : Effect()
	}
}
