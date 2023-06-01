package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object UpdatePassword {
	sealed class State : ViewState {
		data class Idle(
			val password: String = "",
			val error: String? = null,
		) : State()

		data class Updating(val password: String) : State()
	}

	sealed class Action : ViewAction {
		class ClickSignIn(val password: String) : Action()
	}

	sealed class Event : ViewEvent {
		object CloseDialog : Event()
		class ShowSnackBar(val message: String) : Event()
	}
}