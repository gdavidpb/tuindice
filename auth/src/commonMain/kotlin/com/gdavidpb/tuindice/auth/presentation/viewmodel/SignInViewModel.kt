package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.presentation.action.SetAnalyticsCollectionEnabledActionProcessor
import com.gdavidpb.tuindice.base.data.source.event.InMemoryAnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.auth.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.TogglePasswordVisibilityActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow

class SignInViewModel(
	private val signInActionProcessor: SignInActionProcessor,
	private val setUsbIdActionProcessor: SetUsbIdActionProcessor,
	private val setPasswordActionProcessor: SetPasswordActionProcessor,
	private val togglePasswordVisibilityActionProcessor: TogglePasswordVisibilityActionProcessor,
	private val openTermsAndConditionsActionProcessor: OpenTermsAndConditionsActionProcessor,
	private val privacyPolicyActionProcessor: OpenPrivacyPolicyActionProcessor,
	private val analyticsConsentRepository: AnalyticsConsentRepository = InMemoryAnalyticsConsentRepository(),
	private val setAnalyticsCollectionEnabledActionProcessor: SetAnalyticsCollectionEnabledActionProcessor =
		SetAnalyticsCollectionEnabledActionProcessor(analyticsConsentRepository),
	override val eventPublisher: EventPublisher
) : BaseViewModel<SignIn.State, SignIn.Action, SignIn.Effect>(
	name = "sign_in",
	initialState = SignIn.State.Idle(
		analyticsCollectionEnabled = analyticsConsentRepository.isAnalyticsCollectionEnabled()
	)
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

	fun setAnalyticsCollectionEnabledAction(enabled: Boolean) =
		sendAction(SignIn.Action.SetAnalyticsCollectionEnabled(enabled))

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

	override suspend fun processAction(
		action: SignIn.Action,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		return when (action) {
			is SignIn.Action.SetUsbId ->
				setUsbIdActionProcessor.process(action, sideEffect)

			is SignIn.Action.SetPassword ->
				setPasswordActionProcessor.process(action, sideEffect)

			is SignIn.Action.TogglePasswordVisibility ->
				togglePasswordVisibilityActionProcessor.process(action, sideEffect)

			is SignIn.Action.SetAnalyticsCollectionEnabled ->
				setAnalyticsCollectionEnabledActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickSignIn ->
				signInActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickTermsAndConditions ->
				openTermsAndConditionsActionProcessor.process(action, sideEffect)

			is SignIn.Action.ClickPrivacyPolicy ->
				privacyPolicyActionProcessor.process(action, sideEffect)
		}
	}
}
