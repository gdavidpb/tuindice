package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class SignInViewModelTest {
	@Test
	fun setUsbIdAndPasswordAction_updatesIdleState() = runBlocking {
		val viewModel = createViewModel()
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.setUsbIdAction("20-32000")
			viewModel.setPasswordAction("secret")

			waitUntil {
				val state = viewModel.state.value
				state is SignIn.State.Idle && state.usbId == "20-32000" && state.password == "secret"
			}

			val state = assertIs<SignIn.State.Idle>(viewModel.state.value)
			assertEquals("20-32000", state.usbId)
			assertEquals("secret", state.password)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun signInAction_whenCredentialsAreValid_emitsNavigateToSummary() = runBlocking {
		val viewModel = createViewModel()
		val effects = mutableListOf<SignIn.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.signInAction(usbId = "20-32000", password = "secret")

			waitUntil { effects.isNotEmpty() }
			assertEquals(SignIn.Effect.NavigateToSummary, effects.single())

			val state = assertIs<SignIn.State.LoggingIn>(viewModel.state.value)
			assertEquals("20-32000", state.usbId)
			assertEquals("secret", state.password)
			assertEquals(listOf("Loading 1", "Loading 2"), state.messages)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun openPolicyActions_emitBrowserEffects() = runBlocking {
		val viewModel = createViewModel()
		val effects = mutableListOf<SignIn.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.openTermsAndConditionsAction()
			viewModel.openPrivacyPolicyAction()

			waitUntil { effects.size == 2 }

			val terms = assertIs<SignIn.Effect.NavigateToBrowser>(effects[0])
			assertEquals("Terms", terms.title)
			assertEquals("https://tuindice.app/terms", terms.url)

			val privacy = assertIs<SignIn.Effect.NavigateToBrowser>(effects[1])
			assertEquals("Privacy", privacy.title)
			assertEquals("https://tuindice.app/privacy", privacy.url)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(): SignInViewModel {
		return SignInViewModel(
			signInActionProcessor = SignInActionProcessor(
				signInUseCase = SignInUseCase(
					sessionRepository = SignInViewModelFakeSessionRepository(),
					authApiRepository = SignInViewModelFakeAuthApiRepository(),
					attestationRepository = SignInViewModelFakeIntegrityGateway(),
					messagingApiRepository = SignInViewModelFakeMessagingApiRepository(),
					reportingRepository = SignInViewModelFakeReportingRepository(),
					messagingRepository = SignInViewModelFakeMessagingRepository(),
						paramsValidator = SignInParamsValidator(),
						exceptionHandler = SignInExceptionHandler(
							networkRepository = SignInViewModelFakeNetworkStatusGateway(),
							reportingRepository = SignInViewModelFakeReportingGateway
						)
					),
				configRepository = SignInViewModelFakeConfigGateway(),
				textProvider = SignInViewModelFakeLoginTextProvider
			),
			setUsbIdActionProcessor = SetUsbIdActionProcessor(),
			setPasswordActionProcessor = SetPasswordActionProcessor(),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				textProvider = SignInViewModelFakeLoginTextProvider,
				appEnvironmentRepository = SignInViewModelFakeAppEnvironmentGateway()
			),
			privacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				textProvider = SignInViewModelFakeLoginTextProvider,
				appEnvironmentRepository = SignInViewModelFakeAppEnvironmentGateway()
			)
		)
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}

private object SignInViewModelFakeLoginTextProvider : LoginTextProvider {
	override fun privacyPolicyTitle(): String = "Privacy"
	override fun termsAndConditionsTitle(): String = "Terms"
	override fun invalidCredentials(): String = "Invalid credentials"
	override fun userDisabled(): String = "User disabled"
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun retry(): String = "Retry"
	override fun timeout(): String = "Timeout"
	override fun passwordUpdated(): String = "Password updated"
	override fun invalidPassword(): String = "Invalid password"
	override fun defaultError(): String = "Default error"
}

private class SignInViewModelFakeSessionRepository : SessionRepository {
	private var usbId: String = ""
	private var accessToken: String = ""
	private var refreshToken: String = ""

	override suspend fun hasActiveSession(): Boolean = accessToken.isNotEmpty()
	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}
	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}
	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}
	override suspend fun getUsbId(): String = usbId
	override suspend fun getAccessToken(): String = accessToken
	override suspend fun getRefreshToken(): String = refreshToken
	override suspend fun clear() {
		usbId = ""
		accessToken = ""
		refreshToken = ""
	}
}

private class SignInViewModelFakeAuthApiRepository : AuthApiRepository {
	override suspend fun issueTokens(usbId: String, password: String, attestation: Attestation): IssueTokens {
		return IssueTokens(
			uid = "uid-123",
			usbId = usbId,
			accessToken = "access-token",
			refreshToken = "refresh-token",
			expiresIn = 3_600L
		)
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	) = throw UnsupportedOperationException("Not used in this test")

	override suspend fun revokeTokens() = Unit
}

private class SignInViewModelFakeIntegrityGateway : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}

private class SignInViewModelFakeMessagingApiRepository : MessagingApiRepository {
	override suspend fun subscribe(token: String) = Unit
}

private class SignInViewModelFakeReportingRepository : ReportingRepository {
	override suspend fun setIdentifier(id: String) = Unit
}

private class SignInViewModelFakeMessagingRepository : MessagingRepository {
	override suspend fun getToken(): String = "push-token"
}

private class SignInViewModelFakeConfigGateway : ConfigRepository {
	override suspend fun tryFetch() = Unit
	override fun getTimeout(): Long = 30_000L
	override fun getContactEmail(): String = "support@tuindice.app"
	override fun getContactSubject(): String = "Support"
	override fun getLoadingMessages(): List<String> = listOf("Loading 1", "Loading 2")
	override fun getTimeUpdateStalenessDays(): Int = 7
	override fun getSyncsToSuggestReview(): Int = 3
}

private class SignInViewModelFakeAppEnvironmentGateway : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment {
		return AppEnvironment(
			apiBaseUrl = "https://api.tuindice.app/",
			privacyPolicyUrl = "https://tuindice.app/privacy",
			termsAndConditionsUrl = "https://tuindice.app/terms",
			debug = false
		)
	}
}

private class SignInViewModelFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private object SignInViewModelFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
