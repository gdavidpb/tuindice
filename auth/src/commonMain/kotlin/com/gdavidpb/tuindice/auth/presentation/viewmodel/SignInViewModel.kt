package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class SignInViewModel(
	override val screenMachine: SignInMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SignIn.State, SignIn.Action, SignIn.Effect>(
	name = "sign_in",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun setUsbIdAction(usbId: String) =
		sendAction(
			SignIn.Action.SetUsbId(
				usbId = usbId
			)
		)

	fun setPasswordAction(password: String) =
		sendAction(
			SignIn.Action.SetPassword(
				password = password
			)
		)

	fun togglePasswordVisibilityAction() =
		sendAction(SignIn.Action.TogglePasswordVisibility)

	fun setUsageDataCollectionEnabledAction(enabled: Boolean) =
		sendAction(SignIn.Action.SetUsageDataCollectionEnabled(enabled))

	fun signInAction() =
		sendAction(SignIn.Action.ClickSignIn)

	fun openTermsAndConditionsAction() =
		sendAction(SignIn.Action.ClickTermsAndConditions)

	fun openPrivacyPolicyAction() =
		sendAction(SignIn.Action.ClickPrivacyPolicy)

}
