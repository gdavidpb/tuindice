package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.mapper.toSignInParams
import com.gdavidpb.tuindice.login.presentation.reducer.SignInReducer

class SignInViewModel(
	private val resourceResolver: ResourceResolver,
	private val signInUseCase: SignInUseCase,
	private val signInReducer: SignInReducer
) : BaseViewModel<SignIn.State, SignIn.Action, SignIn.Event>(initialViewState = SignIn.State.Idle()) {

	fun signInAction() {
		val currentState = getCurrentState()

		if (currentState is SignIn.State.Idle) {
			val (usbId, password) = currentState

			emitAction(SignIn.Action.ClickSignIn(usbId, password))
		}
	}

	fun openTermsAndConditionsAction() =
		emitAction(SignIn.Action.OpenTermsAndConditions)

	fun openPrivacyPolicyAction() =
		emitAction(SignIn.Action.OpenPrivacyPolicy)

	override suspend fun reducer(action: SignIn.Action) {
		when (action) {
			is SignIn.Action.ClickSignIn ->
				signInUseCase
					.execute(params = action.toSignInParams())
					.collect(viewModel = this, reducer = signInReducer)

			is SignIn.Action.OpenTermsAndConditions ->
				sendEvent(
					SignIn.Event.NavigateToBrowser(
						title = resourceResolver.getString(R.string.label_terms_and_conditions),
						url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
					)
				)

			is SignIn.Action.OpenPrivacyPolicy ->
				sendEvent(
					SignIn.Event.NavigateToBrowser(
						title = resourceResolver.getString(R.string.label_privacy_policy),
						url = BuildConfig.URL_APP_PRIVACY_POLICY
					)
				)
		}
	}
}