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
			// How many times the person switched the identifier mode by hand. The field starts over (it
			// adopts the state's text) only when this changes; the automatic switch to email on an @ must
			// not, or an echo that lags behind the keys would erase what was typed after it.
			val identifierToggleCount: Int = 0,
			val isPasswordVisible: Boolean = false,
			val usageDataCollectionEnabled: Boolean = false,
			// The university's services asked for a wait: sign-in stays disabled until it elapses.
			val isServiceUnavailable: Boolean = false,
			// The backend rejected the last sign-in attempt and nothing has been edited since. It says
			// which verdict it was, because each is shown differently. A failure on the way (no
			// connection, timeout, a wait asked by the service) is not a rejection.
			val rejection: Rejection? = null
		) : State()

		data class LoggingIn(
			val usbId: String,
			val password: String,
			val messages: List<String>,
			val identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
			val usageDataCollectionEnabled: Boolean = false
		) : State()
	}

	// The backend's verdict on the last attempt, with the message to show for it.
	sealed class Rejection {
		abstract val message: String

		// Marks both fields and explains itself under the password.
		data class InvalidCredentials(override val message: String) : Rejection()

		// Terminal verdicts that point at support: fixed under the button, the fields stay as they are.
		data class AccountDisabled(override val message: String) : Rejection()

		data class Untrusted(override val message: String) : Rejection()
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

		data object ClickCancelSignIn : Action()

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
