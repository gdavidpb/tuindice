package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Enrollment {
	sealed class State : ViewState {
		data object Fetching : State()
	}

	sealed class Action : ViewAction {
		data object FetchEnrollmentProof : Action()
	}

	sealed class Effect : ViewEffect {
		data object CloseDialog : Effect()
		class OpenEnrollmentProof(val path: String) : Effect()
		data object NavigateToOutdatedPassword : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}