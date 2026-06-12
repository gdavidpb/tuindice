package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import ru.nsk.kstatemachine.event.Event

sealed interface SignInMachineEvent : Event {
	data class SetUsbId(
		val usbId: String
	) : SignInMachineEvent

	data class SetPassword(
		val password: String
	) : SignInMachineEvent

	data object TogglePasswordVisibility : SignInMachineEvent

	data class SetUsageDataCollectionEnabled(
		val enabled: Boolean
	) : SignInMachineEvent

	data class ClickSignIn(
		val usbId: String,
		val password: String
	) : SignInMachineEvent

	data object ClickTermsAndConditions : SignInMachineEvent

	data object ClickPrivacyPolicy : SignInMachineEvent

	data object SignInSucceeded : SignInMachineEvent

	data class SignInFailed(
		val error: SignInUseCaseError?
	) : SignInMachineEvent
}
