package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object SignIn {
	sealed class State : ViewState {
		data class Idle(
			val usbId: String = "",
			val password: String = ""
		) : State()

		data class LoggingIn(
			val usbId: String,
			val password: String,
			val messages: List<String>
		) : State()

		object LoggedIn : State()
	}

	sealed class Action : ViewAction {
		class ClickSignIn(val usbId: String, val password: String) : Action()
		object OpenTermsAndConditions : Action()
		object OpenPrivacyPolicy : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToSummary : Event()

		class NavigateToBrowser(
			val title: String,
			val url: String
		) : Event()

		class ShowSnackBar(
			val message: String,
			val actionLabel: String? = null,
			val actionId: Int? = null
		) : Event()
	}
}