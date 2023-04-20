package com.gdavidpb.tuindice.login.presentation.contract

object UpdatePassword {
	sealed class State {
		object Idle : State()
		object LoggingIn : State()
		object LoggedIn : State()
	}

	sealed class Action {
		object CloseDialog : Action()
		class ClickSignIn(val password: String) : Action()
	}

	sealed class Event {
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