package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object UpdatePassword {
	sealed class State : ViewState {
		data class Idle(
			val password: String = "",
			val error: String? = null,
		) : State()

		data class Updating(val password: String) : State()
	}

	sealed class Action : ViewAction {
		class SetPassword(val password: String) : Action()
		class ClickSignIn(val password: String) : Action()
	}

	sealed class Effect : ViewEffect {
		data object CloseDialog : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}