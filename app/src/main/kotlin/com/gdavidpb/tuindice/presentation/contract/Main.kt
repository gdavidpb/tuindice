package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

object Main {
	sealed class State : ViewState {
		object Starting : State()

		data class Started(
			val title: String,
			val startDestination: Destination,
			val currentDestination: Destination,
			val destinations: HashMap<String, Destination>
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		class StartUp(val data: String?) : Action()
		class RequestReview(val reviewManager: ReviewManager) : Action()
		class RequestUpdate(val appUpdateManager: AppUpdateManager) : Action()
		class SetLastScreen(val route: String) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowNoServicesDialog(val status: ServicesStatus) : Event()
		class ShowReviewDialog(val reviewInfo: ReviewInfo) : Event()
		class StartUpdateFlow(val updateInfo: AppUpdateInfo) : Event()
	}
}