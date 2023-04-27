package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.reducer.SignInReducer

class SignInViewModel(
	private val signInReducer: SignInReducer
) : BaseViewModel<SignIn.State, SignIn.Action, SignIn.Event>(initialViewState = SignIn.State.Idle) {

	fun signInAction(params: SignInParams) =
		emitAction(SignIn.Action.ClickSignIn(usbId = params.usbId, password = params.password))

	fun tapLogoAction() =
		emitAction(SignIn.Action.ClickLogo)

	fun openTermsAndConditions() =
		emitAction(SignIn.Action.OpenTermsAndConditions)

	fun openPrivacyPolicy() =
		emitAction(SignIn.Action.OpenPrivacyPolicy)

	override suspend fun reducer( action: SignIn.Action) {
		when (action) {
			is SignIn.Action.ClickSignIn ->
				signInReducer.reduce(
					action = action,
					stateProvider = ::getCurrentState,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is SignIn.Action.ClickLogo ->
				sendEvent(SignIn.Event.ShakeLogo)

			is SignIn.Action.OpenTermsAndConditions ->
				sendEvent(SignIn.Event.NavigateToTermsAndConditions)

			is SignIn.Action.OpenPrivacyPolicy ->
				sendEvent(SignIn.Event.NavigateToPrivacyPolicy)
		}
	}
}