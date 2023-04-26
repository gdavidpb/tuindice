package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

object Enrollment {
	sealed class State {
		object Fetching : State()
		object Fetched : State()
		object Failed : State()
	}

	sealed class Action {
		object FetchEnrollmentProof : Action()
	}

	sealed class Event {
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