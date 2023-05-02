package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

import com.gdavidpb.tuindice.base.presentation.reducer.ViewAction
import com.gdavidpb.tuindice.base.presentation.reducer.ViewEvent
import com.gdavidpb.tuindice.base.presentation.reducer.ViewState

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
		object NavigateToAccountDisabled : Event()
		object NavigateToOutdatedPassword : Event()
		object ShowTimeoutSnackBar : Event()
		object ShowNotFoundSnackBar : Event()
		object ShowUnsupportedFileSnackBar : Event()
		object ShowUnavailableSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowDefaultErrorError : Event()
	}
}