package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object UpdatePassword {
	sealed class State : ViewState {
		object Idle : State()
		object LoggingIn : State()
		object LoggedIn : State()
	}

	sealed class Action : ViewAction {
		object CloseDialog : Action()
		class ClickSignIn(val password: String) : Action()
	}

	sealed class Event : ViewEvent {
		object CloseDialog : Event()
		object HideSoftKeyboard : Event()
		object NavigateToAccountDisabled : Event()
		object ShowPasswordUpdatedToast : Event()
		object ShowPasswordEmptyError : Event()
		object ShowTimeoutError : Event()
		class ShowNoConnectionError(val isNetworkAvailable: Boolean) : Event()
		object ShowUnavailableError : Event()
		object ShowInvalidCredentialsError : Event()
		object ShowDefaultErrorError : Event()
	}
}