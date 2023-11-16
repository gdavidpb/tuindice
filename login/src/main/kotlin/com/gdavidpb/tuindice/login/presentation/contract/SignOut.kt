package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object SignOut {
	sealed class State : ViewState {
		data object Idle : State()
		data object LoggingOut : State()
	}

	sealed class Action : ViewAction {
		data object ConfirmSignOut : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToSignIn : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}