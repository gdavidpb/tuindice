package com.gdavidpb.tuindice.auth.presentation.contract

import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.top_bar_tuindice

object SignIn {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_tuindice),
		override val isTopBarVisible: Boolean = true
	) : ViewState {
		data class Idle(
			val usbId: String = "",
			val password: String = "",
			val identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
			val isPasswordVisible: Boolean = false,
			val usageDataCollectionEnabled: Boolean = false
		) : State()

		data class LoggingIn(
			val usbId: String,
			val password: String,
			val messages: List<String>,
			val identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
			val usageDataCollectionEnabled: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction {
		class SetUsbId(
			val usbId: String
		) : Action()

		class SetPassword(
			val password: String
		) : Action()

		data object TogglePasswordVisibility : Action()

		data object ToggleIdentifierMode : Action()

		class SetUsageDataCollectionEnabled(
			val enabled: Boolean
		) : Action()

		data object ClickSignIn : Action()

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
			val actionLabel: String
		) : Effect()

		data object ShowOutdatedApp : Effect()
	}
}
