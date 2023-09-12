package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object SignOut {
	sealed class State : ViewState {
		object Idle : State()
		object LoggingOut : State()
	}

	sealed class Action : ViewAction {
		object ConfirmSignOut : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToSignIn : Event()
	}
}