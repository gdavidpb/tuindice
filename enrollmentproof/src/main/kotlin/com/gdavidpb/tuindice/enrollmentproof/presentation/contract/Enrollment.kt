package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object Enrollment {
	sealed class State : ViewState {
		object Fetching : State()
		object Fetched : State()
		object Failed : State()
	}

	sealed class Action : ViewAction {
		object FetchEnrollmentProof : Action()
	}

	sealed class Event : ViewEvent {
		object CloseDialog : Event()
		class OpenEnrollmentProof(val path: String) : Event()
		object NavigateToOutdatedPassword : Event()
		object ShowTimeoutSnackBar : Event()
		object ShowNotFoundSnackBar : Event()
		object ShowUnsupportedFileSnackBar : Event()
		object ShowUnavailableSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowDefaultErrorError : Event()
	}
}