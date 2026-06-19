package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.mapper.toErrorMessage
import com.gdavidpb.tuindice.auth.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.idleTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.loggingInTransitions
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.label_retry
import tuindice.auth.generated.resources.title_privacy_policy
import tuindice.auth.generated.resources.title_terms_and_conditions

class SignInMachine(
	private val signInUseCase: SignInUseCase,
	private val configRepository: ConfigRepository,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	private val usageDataConsentRepository: UsageDataConsentRepository
) : ScreenMachine<SignIn.State, SignIn.Effect> {
	override fun initialState(): SignIn.State {
		return SignIn.State.Idle(
			usageDataCollectionEnabled = usageDataConsentRepository.isUsageDataCollectionEnabled()
		)
	}

	override fun define(host: MachineHost<SignIn.Effect>): MachineDefinition<SignIn.State> {
		return MachineDefinition.define {
			idleTransitions(machine = this@SignInMachine, host = host)
			loggingInTransitions(machine = this@SignInMachine, host = host)
			anyStateTransitions(machine = this@SignInMachine, host = host)
		}
	}

	internal fun startSignIn(
		host: MachineHost<SignIn.Effect>,
		state: SignIn.State.Idle
	): SignIn.State.LoggingIn {
		val params = SignInParams(
			usbId = state.usbId,
			password = state.password,
			identifierMode = state.identifierMode
		)

		host.launchMachineJob {
			signInUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SignInInternalEvent.SignInSucceeded
					)

					is UseCaseState.Error ->
						if (useCaseState.error is SignInUseCaseError.OutdatedApp) {
							host.processInternalEvent(SignInInternalEvent.OutdatedAppDetected)
						} else {
							host.processInternalEvent(
								SignInInternalEvent.SignInFailed(error = useCaseState.error)
							)
						}
				}
			}
		}

		return SignIn.State.LoggingIn(
			usbId = state.usbId,
			password = state.password,
			messages = configRepository.getLoadingMessages(),
			identifierMode = state.identifierMode,
			usageDataCollectionEnabled = state.usageDataCollectionEnabled
		)
	}

	internal suspend fun failSignIn(
		host: MachineHost<SignIn.Effect>,
		state: SignIn.State.LoggingIn,
		event: SignInInternalEvent.SignInFailed
	): SignIn.State.Idle {
		val error = event.error
		val errorMessage = error.toErrorMessage(identifierMode = state.identifierMode)

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
						actionLabel = getString(Res.string.label_retry)
					)
				)
		}

		return SignIn.State.Idle(
			usbId = state.usbId,
			password = state.password,
			identifierMode = state.identifierMode,
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

	internal suspend fun requestStartupGate(host: MachineHost<SignIn.Effect>) {
		host.sendEffect(SignIn.Effect.RequestStartupGate)
	}
}
