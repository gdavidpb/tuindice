package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInInternalEvent
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
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

class SignInViewModel(
	private val signInUseCase: SignInUseCase,
	private val configRepository: ConfigRepository,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	private val usageDataConsentRepository: UsageDataConsentRepository = InMemoryUsageDataConsentRepository(),
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SignIn.State, SignIn.Action, SignIn.Effect>(
	name = "sign_in",
	initialState = SignIn.State.Idle(
		usageDataCollectionEnabled = usageDataConsentRepository.isUsageDataCollectionEnabled()
	),
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

	override fun defineMachine() = MachineDefinition.define {
		from<SignIn.State.Idle> {
			on<SignIn.Action.SetUsbId> { state, action ->
				state.copy(usbId = action.usbId)
			}

			on<SignIn.Action.SetPassword> { state, action ->
				state.copy(password = action.password)
			}

			on<SignIn.Action.TogglePasswordVisibility> { state, _ ->
				state.copy(isPasswordVisible = !state.isPasswordVisible)
			}

			onTo<SignIn.Action.ClickSignIn, SignIn.State.LoggingIn> { state, action ->
				startSignIn(state = state, action = action)
			}
		}

		from<SignIn.State.LoggingIn> {
			on<SignInInternalEvent.SignInSucceeded> { state, _ ->
				sendEffect(SignIn.Effect.NavigateToSummary)
				state
			}

			onTo<SignInInternalEvent.SignInFailed, SignIn.State.Idle> { state, event ->
				failSignIn(state = state, event = event)
			}
		}

		fromAny {
			on<SignIn.Action.ClickTermsAndConditions> { state, _ ->
				sendEffect(
					SignIn.Effect.NavigateToBrowser(
						title = getString(Res.string.title_terms_and_conditions),
						url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
					)
				)

				state
			}

			on<SignIn.Action.ClickPrivacyPolicy> { state, _ ->
				sendEffect(
					SignIn.Effect.NavigateToBrowser(
						title = getString(Res.string.title_privacy_policy),
						url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
					)
				)

				state
			}

			on<SignIn.Action.SetUsageDataCollectionEnabled> { state, action ->
				usageDataConsentRepository.setUsageDataCollectionEnabled(action.enabled)

				state.withUsageDataCollection(enabled = action.enabled)
			}
		}
	}

	private fun SignIn.State.withUsageDataCollection(enabled: Boolean): SignIn.State {
		return when (this) {
			is SignIn.State.Idle -> copy(usageDataCollectionEnabled = enabled)
			is SignIn.State.LoggingIn -> copy(usageDataCollectionEnabled = enabled)
		}
	}

	private fun startSignIn(
		state: SignIn.State.Idle,
		action: SignIn.Action.ClickSignIn
	): SignIn.State.LoggingIn {
		val params = SignInParams(
			usbId = action.usbId,
			password = action.password
		)

		launchMachineJob {
			signInUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> processInternalEvent(
						SignInInternalEvent.SignInSucceeded
					)

					is UseCaseState.Error -> processInternalEvent(
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

	private suspend fun failSignIn(
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
				sendEffect(
					SignIn.Effect.ShowSnackBar(
						message = errorMessage
					)
				)

			else ->
				sendEffect(
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
}
