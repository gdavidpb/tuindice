package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object SignIn {
	sealed class State : ViewState {
		object Idle : State()
		class LoggingIn(val messages: List<String>) : State()
		object LoggedIn : State()
	}

	sealed class Action : ViewAction {
		class ClickSignIn(val usbId: String, val password: String) : Action()
		object ClickLogo : Action()
		object OpenTermsAndConditions : Action()
		object OpenPrivacyPolicy : Action()
	}

	sealed class Event : ViewEvent {
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