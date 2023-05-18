package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

object Main {
	sealed class State : ViewState {
		object Idle : State()
	}

	sealed class Action : ViewAction {
		class RequestReview(val reviewManager: ReviewManager) : Action()
		class RequestUpdate(val appUpdateManager: AppUpdateManager) : Action()
		class SetLastScreen(val screen: Int) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowReviewDialog(val reviewInfo: ReviewInfo) : Event()
		class StartUpdateFlow(val updateInfo: AppUpdateInfo) : Event()
	}
}