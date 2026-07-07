package com.gdavidpb.tuindice.auth.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object UpdatePassword {
	sealed class State : ViewState {
		data class Idle(
			val password: String = "",
			val isPasswordVisible: Boolean = false,
			val error: String? = null,
		) : State()

		data class Updating(
			val password: String,
			val isPasswordVisible: Boolean = false,
		) : State()
	}

	sealed class Action : ViewAction {
		class SetPassword(val password: String) : Action()
		data object TogglePasswordVisibility : Action()
		data object ClickSignIn : Action()
	}

	sealed class Effect : ViewEffect {
		class PasswordUpdated(val message: String) : Effect()
	}
}
