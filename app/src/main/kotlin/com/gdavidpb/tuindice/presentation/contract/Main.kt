package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

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
		class RequestReview(val reviewManager: ReviewManager) : Action()
		class RequestUpdate(val appUpdateManager: AppUpdateManager) : Action()
		class SetLastDestination(val destination: Destination) : Action()
	}

	sealed class Effect : ViewEffect() {
		class NavigateToGooglePlayServicesUnavailableDialog(val status: ServicesStatus) : Effect()
		class NavigateToReviewDialog(val reviewInfo: ReviewInfo) : Effect()
		class StartUpdateFlow(val updateInfo: AppUpdateInfo) : Effect()
	}
}