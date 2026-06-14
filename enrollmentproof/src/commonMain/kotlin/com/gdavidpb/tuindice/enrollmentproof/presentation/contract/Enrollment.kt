package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import io.github.vinceglb.filekit.PlatformFile

object Enrollment {
	sealed class State : ViewState {
		data object Fetching : State()
	}

	sealed class Action : ViewAction {
		data object FetchEnrollmentProof : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToOutdatedCredentials : Effect()
		class OpenEnrollmentProof(val file: PlatformFile) : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}
