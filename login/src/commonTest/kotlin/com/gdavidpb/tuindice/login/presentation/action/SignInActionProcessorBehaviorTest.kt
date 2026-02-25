package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository as BaseReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignInActionProcessorBehaviorTest {
	@Test
	fun process_whenSignInSucceeds_emitsNavigateToSummary() = runBlocking {
		val sessionRepository = SignInBehaviorFakeSessionRepository()
		val processor = createProcessor(
			authApiRepository = SignInBehaviorFakeAuthApiRepository(),
			sessionRepository = sessionRepository
		)
		val effects = mutableListOf<SignIn.Effect>()

		val mutations = processor.process(
			action = SignIn.Action.ClickSignIn(
				usbId = "20-32000",
				password = "secret"
			),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = SignIn.State.Idle(),
			mutations = mutations
		)

		val loggingIn = assertIs<SignIn.State.LoggingIn>(finalState)
		assertEquals("20-32000", loggingIn.usbId)
		assertEquals("secret", loggingIn.password)
		assertEquals(listOf("Loading 1", "Loading 2"), loggingIn.messages)
		assertEquals(SignIn.Effect.NavigateToSummary, effects.single())

		assertEquals("20-32000", sessionRepository.getUsbId())
		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
	}

	@Test
	fun process_whenSignInFails_emitsRetrySnackBarAndReturnsIdle() = runBlocking {
		val processor = createProcessor(
			authApiRepository = SignInBehaviorFakeAuthApiRepository(
				issueTokensThrowable = IllegalStateException("boom")
			)
		)
		val effects = mutableListOf<SignIn.Effect>()

		val mutations = processor.process(
			action = SignIn.Action.ClickSignIn(
				usbId = "20-32000",
				password = "secret"
			),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = SignIn.State.Idle(),
			mutations = mutations
		)

		val idle = assertIs<SignIn.State.Idle>(finalState)
		assertEquals("20-32000", idle.usbId)
		assertEquals("secret", idle.password)

		val retrySnackBar = assertIs<SignIn.Effect.ShowRetrySnackBar>(effects.single())
		assertEquals("Default error", retrySnackBar.message)
		assertEquals("Retry", retrySnackBar.actionLabel)
		assertEquals(SignInParams(usbId = "20-32000", password = "secret"), retrySnackBar.params)
	}

	private fun createProcessor(
		authApiRepository: SignInBehaviorFakeAuthApiRepository,
		sessionRepository: SignInBehaviorFakeSessionRepository = SignInBehaviorFakeSessionRepository()
	): SignInActionProcessor {
		return SignInActionProcessor(
			signInUseCase = SignInUseCase(
				sessionRepository = sessionRepository,
				authApiRepository = authApiRepository,
				attestationRepository = SignInBehaviorFakeIntegrityGateway(),
				messagingApiRepository = SignInBehaviorFakeMessagingApiRepository(),
				reportingRepository = SignInBehaviorFakeReportingRepository(),
				messagingRepository = SignInBehaviorFakeMessagingRepository(),
				paramsValidator = SignInParamsValidator(),
				exceptionHandler = SignInExceptionHandler(
					networkRepository = SignInBehaviorFakeNetworkStatusGateway(),
					reportingRepository = SignInBehaviorFakeReportingGateway
				)
			),
			configRepository = SignInBehaviorFakeConfigGateway(),
			textProvider = SignInBehaviorFakeLoginTextProvider
		)
	}

	private fun applyMutations(
		initialState: SignIn.State,
		mutations: List<(SignIn.State) -> SignIn.State>
	): SignIn.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object SignInBehaviorFakeLoginTextProvider : LoginTextProvider {
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

private class SignInBehaviorFakeSessionRepository : SessionRepository {
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

private class SignInBehaviorFakeAuthApiRepository(
	private val issueTokensThrowable: Throwable? = null
) : AuthApiRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		issueTokensThrowable?.let { throw it }

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

private class SignInBehaviorFakeIntegrityGateway : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}

private class SignInBehaviorFakeMessagingApiRepository : MessagingApiRepository {
	override suspend fun subscribe(token: String) = Unit
}

private class SignInBehaviorFakeReportingRepository : LoginReportingRepository {
	override suspend fun setIdentifier(id: String) = Unit
}

private class SignInBehaviorFakeMessagingRepository : MessagingRepository {
	override suspend fun getToken(): String = "push-token"
}

private class SignInBehaviorFakeConfigGateway : ConfigRepository {
	override suspend fun tryFetch() = Unit
	override fun getTimeout(): Long = 30_000L
	override fun getContactEmail(): String = "support@tuindice.app"
	override fun getContactSubject(): String = "Support"
	override fun getLoadingMessages(): List<String> = listOf("Loading 1", "Loading 2")
	override fun getTimeUpdateStalenessDays(): Int = 7
	override fun getSyncsToSuggestReview(): Int = 3
}

private class SignInBehaviorFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private object SignInBehaviorFakeReportingGateway : BaseReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
