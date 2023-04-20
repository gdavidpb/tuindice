package com.gdavidpb.tuindice.login.presentation.contract

object SignIn {
	sealed class State {
		object Idle : State()
		class LoggingIn(val messages: List<String>) : State()
		object LoggedIn : State()
	}

	sealed class Action {
		class ClickSignIn(val usbId: String, val password: String) : Action()
		object ClickLogo : Action()
		object OpenTermsAndConditions : Action()
		object OpenPrivacyPolicy : Action()
	}

	sealed class Event {
		object NavigateToSplash : Event()
		object NavigateToTermsAndConditions : Event()
		object NavigateToPrivacyPolicy : Event()
		object HideSoftKeyboard : Event()
		object ShakeLogo : Event()
		object ShowUsbIdEmptyError : Event()
		object ShowUsbIdInvalidError : Event()
		object ShowPasswordEmptyError : Event()
		object ShowTimeoutSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowUnavailableSnackBar : Event()
		object ShowAccountDisabledSnackBar : Event()
		object ShowInvalidCredentialsSnackBar : Event()
		object ShowDefaultErrorSnackBar : Event()
	}
}