package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.idleTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.loggingInTransitions
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_credentials
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.label_retry
import tuindice.auth.generated.resources.snack_default_error
import tuindice.auth.generated.resources.snack_network_unavailable
import tuindice.auth.generated.resources.snack_sign_in_failed
import tuindice.auth.generated.resources.snack_service_unavailable
import tuindice.auth.generated.resources.snack_timeout
import tuindice.auth.generated.resources.title_privacy_policy
import tuindice.auth.generated.resources.title_terms_and_conditions

class SignInMachine(
	private val signInUseCase: SignInUseCase,
	private val configRepository: ConfigRepository,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	private val usageDataConsentRepository: UsageDataConsentRepository
) {
	fun define(host: MachineHost<SignIn.Effect>): MachineDefinition<SignIn.State> {
		return MachineDefinition.define {
			idleTransitions(machine = this@SignInMachine, host = host)
			loggingInTransitions(machine = this@SignInMachine, host = host)
			anyStateTransitions(machine = this@SignInMachine, host = host)
		}
	}

	internal fun startSignIn(
		host: MachineHost<SignIn.Effect>,
		state: SignIn.State.Idle,
		action: SignIn.Action.ClickSignIn
	): SignIn.State.LoggingIn {
		val params = SignInParams(
			usbId = action.usbId,
			password = action.password
		)

		host.launchMachineJob {
			signInUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SignInInternalEvent.SignInSucceeded
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SignInInternalEvent.SignInFailed(error = useCaseState.error)
					)
				}
			}
		}

		return SignIn.State.LoggingIn(
			usbId = action.usbId,
			password = action.password,
			messages = configRepository.getLoadingMessages(),
			usageDataCollectionEnabled = state.usageDataCollectionEnabled
		)
	}

	internal suspend fun failSignIn(
		host: MachineHost<SignIn.Effect>,
		state: SignIn.State.LoggingIn,
		event: SignInInternalEvent.SignInFailed
	): SignIn.State.Idle {
		val error = event.error
		val errorMessage = when (error) {
			is SignInUseCaseError.InvalidCredentials ->
				getString(Res.string.error_invalid_credentials)

			is SignInUseCaseError.AccountDisabled ->
				getString(Res.string.error_account_disabled)

			is SignInUseCaseError.Untrusted ->
				getString(Res.string.error_untrusted)

			is SignInUseCaseError.AuthenticationFailed ->
				getString(Res.string.snack_sign_in_failed)

			is SignInUseCaseError.NoConnection ->
				if (error.isNetworkAvailable)
					getString(Res.string.snack_service_unavailable)
				else
					getString(Res.string.snack_network_unavailable)

			is SignInUseCaseError.Timeout ->
				getString(Res.string.snack_timeout)

			is SignInUseCaseError.Unavailable ->
				getString(Res.string.snack_service_unavailable)

			else ->
				getString(Res.string.snack_default_error)
		}

		when (error) {
			is SignInUseCaseError.InvalidCredentials,
			is SignInUseCaseError.AccountDisabled,
			is SignInUseCaseError.Untrusted ->
				host.sendEffect(
					SignIn.Effect.ShowSnackBar(
						message = errorMessage
					)
				)

			else ->
				host.sendEffect(
					SignIn.Effect.ShowRetrySnackBar(
						message = errorMessage,
						actionLabel = getString(Res.string.label_retry),
						params = SignInParams(
							usbId = state.usbId,
							password = state.password
						)
					)
				)
		}

		return SignIn.State.Idle(
			usbId = state.usbId,
			password = state.password,
			usageDataCollectionEnabled = state.usageDataCollectionEnabled
		)
	}

	internal suspend fun openTermsAndConditions(host: MachineHost<SignIn.Effect>) {
		host.sendEffect(
			SignIn.Effect.NavigateToBrowser(
				title = getString(Res.string.title_terms_and_conditions),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)
	}

	internal suspend fun openPrivacyPolicy(host: MachineHost<SignIn.Effect>) {
		host.sendEffect(
			SignIn.Effect.NavigateToBrowser(
				title = getString(Res.string.title_privacy_policy),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)
	}

	internal fun persistUsageDataCollection(enabled: Boolean) {
		usageDataConsentRepository.setUsageDataCollectionEnabled(enabled)
	}
}
