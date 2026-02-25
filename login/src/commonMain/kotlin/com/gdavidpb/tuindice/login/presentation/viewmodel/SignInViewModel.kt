package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow

class SignInViewModel(
	private val signInActionProcessor: SignInActionProcessor,
	private val setUsbIdActionProcessor: SetUsbIdActionProcessor,
	private val setPasswordActionProcessor: SetPasswordActionProcessor,
	private val openTermsAndConditionsActionProcessor: OpenTermsAndConditionsActionProcessor,
	private val privacyPolicyActionProcessor: OpenPrivacyPolicyActionProcessor
) : BaseViewModel<SignIn.State, SignIn.Action, SignIn.Effect>(initialState = SignIn.State.Idle()) {

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

	fun signInAction(usbId: String, password: String) =
		sendAction(
			SignIn.Action.ClickSignIn(
				usbId = usbId,
				password = password
			)
		)

	fun openTermsAndConditionsAction() =
		sendAction(SignIn.Action.ClickTermsAndConditions)

	fun openPrivacyPolicyAction() =
		sendAction(SignIn.Action.ClickPrivacyPolicy)

	override fun processAction(
		action: SignIn.Action,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		return when (action) {
			is SignIn.Action.SetUsbId ->
				setUsbIdActionProcessor.process(action, sideEffect)

			is SignIn.Action.SetPassword ->
				setPasswordActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickSignIn ->
				signInActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickTermsAndConditions ->
				openTermsAndConditionsActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickPrivacyPolicy ->
				privacyPolicyActionProcessor.process(action, sideEffect)
		}
	}
}