package com.gdavidpb.tuindice.login.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams

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
	}

	sealed class Action : ViewAction {
		class SetUsbId(
			val usbId: String
		) : Action()

		class SetPassword(
			val password: String
		) : Action()

		class ClickSignIn(
			val usbId: String,
			val password: String
		) : Action()

		data object ClickTermsAndConditions : Action()

		data object ClickPrivacyPolicy : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToSummary : Effect()

		class NavigateToBrowser(
			val title: String,
			val url: String
		) : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()

		class ShowRetrySnackBar(
			val message: String,
			val actionLabel: String,
			val params: SignInParams
		) : Effect()
	}
}