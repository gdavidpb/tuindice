package com.gdavidpb.tuindice.enrollmentproof.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import io.github.vinceglb.filekit.PlatformFile

object Enrollment {
	sealed class State : ViewState {
		data object Fetching : State()

		// The fresh proof could not be downloaded and a saved copy exists: opening it is the
		// user's call, asked before the viewer covers the app.
		data class ConfirmingSavedCopy(val file: PlatformFile) : State()
	}

	sealed class Action : ViewAction {
		data object FetchEnrollmentProof : Action()
		data object OpenSavedEnrollmentProof : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToOutdatedCredentials : Effect()
		class OpenEnrollmentProof(val file: PlatformFile) : Effect()
		class ShowSnackBar(val message: String, val canRetry: Boolean = true) : Effect()
	}
}
