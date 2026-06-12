package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachineEvent
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import kotlinx.coroutines.Job
import org.jetbrains.compose.resources.getString
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.state.initialState
import ru.nsk.kstatemachine.state.onExit
import ru.nsk.kstatemachine.state.state
import ru.nsk.kstatemachine.state.transition
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.transition.onTriggered
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
	private var signInJob: Job? = null

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

	override fun toMachineEvent(action: SignIn.Action): Event {
		return when (action) {
			is SignIn.Action.SetUsbId ->
				SignInMachineEvent.SetUsbId(usbId = action.usbId)

			is SignIn.Action.SetPassword ->
				SignInMachineEvent.SetPassword(password = action.password)

			is SignIn.Action.TogglePasswordVisibility ->
				SignInMachineEvent.TogglePasswordVisibility

			is SignIn.Action.SetUsageDataCollectionEnabled ->
				SignInMachineEvent.SetUsageDataCollectionEnabled(enabled = action.enabled)

			is SignIn.Action.ClickSignIn ->
				SignInMachineEvent.ClickSignIn(
					usbId = action.usbId,
					password = action.password
				)

			is SignIn.Action.ClickTermsAndConditions ->
				SignInMachineEvent.ClickTermsAndConditions

			is SignIn.Action.ClickPrivacyPolicy ->
				SignInMachineEvent.ClickPrivacyPolicy
		}
	}

	override suspend fun createMachine(): StateMachine {
		return buildMachine {
			val idleState = initialState("idle")
			val loggingInState = state("logging_in")

			transition<SignInMachineEvent.ClickTermsAndConditions> {
				onTriggered {
					sendEffect(
						SignIn.Effect.NavigateToBrowser(
							title = getString(Res.string.title_terms_and_conditions),
							url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
						)
					)
				}
			}

			transition<SignInMachineEvent.ClickPrivacyPolicy> {
				onTriggered {
					sendEffect(
						SignIn.Effect.NavigateToBrowser(
							title = getString(Res.string.title_privacy_policy),
							url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
						)
					)
				}
			}

			transition<SignInMachineEvent.SetUsageDataCollectionEnabled> {
				onTriggered { params ->
					val enabled = params.event.enabled

					usageDataConsentRepository.setUsageDataCollectionEnabled(enabled)

					updateState { state ->
						when (state) {
							is SignIn.State.Idle ->
								state.copy(usageDataCollectionEnabled = enabled)

							is SignIn.State.LoggingIn ->
								state.copy(usageDataCollectionEnabled = enabled)
						}
					}
				}
			}

			idleState.apply {
				transition<SignInMachineEvent.SetUsbId> {
					onTriggered { params ->
						updateIdle { state ->
							state.copy(usbId = params.event.usbId)
						}
					}
				}

				transition<SignInMachineEvent.SetPassword> {
					onTriggered { params ->
						updateIdle { state ->
							state.copy(password = params.event.password)
						}
					}
				}

				transition<SignInMachineEvent.TogglePasswordVisibility> {
					onTriggered {
						updateIdle { state ->
							state.copy(isPasswordVisible = !state.isPasswordVisible)
						}
					}
				}

				transition<SignInMachineEvent.ClickSignIn> {
					targetState = loggingInState

					onTriggered { params ->
						startSignIn(event = params.event)
					}
				}
			}

			loggingInState.apply {
				transition<SignInMachineEvent.SignInSucceeded> {
					onTriggered {
						sendEffect(SignIn.Effect.NavigateToSummary)
					}
				}

				transition<SignInMachineEvent.SignInFailed> {
					targetState = idleState

					onTriggered { params ->
						notifySignInFailed(error = params.event.error)

						updateLoggingIn { state ->
							SignIn.State.Idle(
								usbId = state.usbId,
								password = state.password,
								usageDataCollectionEnabled = state.usageDataCollectionEnabled
							)
						}
					}
				}

				onExit {
					signInJob?.cancel()
					signInJob = null
				}
			}
		}
	}

	private fun startSignIn(event: SignInMachineEvent.ClickSignIn) {
		val params = SignInParams(
			usbId = event.usbId,
			password = event.password
		)

		updateIdle { state ->
			SignIn.State.LoggingIn(
				usbId = event.usbId,
				password = event.password,
				messages = configRepository.getLoadingMessages(),
				usageDataCollectionEnabled = state.usageDataCollectionEnabled
			)
		}

		signInJob = launchMachineJob {
			signInUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> processInternalEvent(
						SignInMachineEvent.SignInSucceeded
					)

					is UseCaseState.Error -> processInternalEvent(
						SignInMachineEvent.SignInFailed(error = useCaseState.error)
					)
				}
			}
		}
	}

	private suspend fun notifySignInFailed(error: SignInUseCaseError?) {
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

			else -> {
				val loggingIn = currentState as? SignIn.State.LoggingIn

				sendEffect(
					SignIn.Effect.ShowRetrySnackBar(
						message = errorMessage,
						actionLabel = getString(Res.string.label_retry),
						params = SignInParams(
							usbId = loggingIn?.usbId.orEmpty(),
							password = loggingIn?.password.orEmpty()
						)
					)
				)
			}
		}
	}

	private fun updateIdle(transform: (SignIn.State.Idle) -> SignIn.State) {
		updateState { state ->
			if (state is SignIn.State.Idle) transform(state) else state
		}
	}

	private fun updateLoggingIn(transform: (SignIn.State.LoggingIn) -> SignIn.State) {
		updateState { state ->
			if (state is SignIn.State.LoggingIn) transform(state) else state
		}
	}
}
