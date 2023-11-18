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
	sealed interface State : ViewState {
		data object Starting : State

		data class Content(
			val title: String,
			val startDestination: Destination,
			val currentDestination: Destination,
			val destinations: Map<String, Destination>,
			val topBarConfig: TopBarConfig? = null
		) : State

		data object Failed : State
	}

	sealed interface Action : ViewAction {
		class UpdateState(val state: State) : Action
		class StartUp(val data: String?) : Action
		class RequestReview(val reviewManager: ReviewManager) : Action
		class RequestUpdate(val appUpdateManager: AppUpdateManager) : Action
		class SetLastScreen(val route: String) : Action
		data object CloseDialog : Action
	}

	sealed interface Effect : ViewEffect {
		class ShowNoServicesDialog(val status: ServicesStatus) : Effect
		class ShowReviewDialog(val reviewInfo: ReviewInfo) : Effect
		class StartUpdateFlow(val updateInfo: AppUpdateInfo) : Effect
		data object CloseDialog : Effect
	}
}