package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.IntegrityGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.IssueTokensAttestationPayload
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStartupWorkflowRegressionTest {
	@Test
	fun signIn_startup_signOut_roundtrip_preservesBusinessContract() = runBlocking {
		val sessionRepository = WorkflowSessionRepository()
		val settingsRepository = WorkflowSettingsRepository(LoginDestination.SignIn)
		val applicationRepository = WorkflowApplicationRepository()
		val dependenciesRepository = WorkflowDependenciesRepository()
		val configGateway = WorkflowConfigGateway()
		val authApiRepository = WorkflowAuthApiRepository()
		val attestationGateway = WorkflowIntegrityGateway()
		val messagingRepository = WorkflowMessagingRepository(token = "push-token-kmp020")
		val messagingApiRepository = WorkflowMessagingApiRepository()
		val loginReportingRepository = WorkflowLoginReportingRepository()
		val pushGateway = WorkflowPushGateway()

		val startUpUseCase = StartUpUseCase(
			sessionRepository = sessionRepository,
			settingsRepository = settingsRepository,
			configRepository = configGateway,
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = applicationRepository,
				reportingRepository = WorkflowReportingGateway()
			)
		)
		val signInUseCase = SignInUseCase(
			sessionRepository = sessionRepository,
			authApiRepository = authApiRepository,
			attestationRepository = attestationGateway,
			messagingApiRepository = messagingApiRepository,
			reportingRepository = loginReportingRepository,
			messagingRepository = messagingRepository,
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = WorkflowNetworkStatusGateway(),
				reportingRepository = WorkflowReportingGateway()
			)
		)
		val setLastDestinationUseCase = SetLastDestinationUseCase(settingsRepository)
		val signOutUseCase = SignOutUseCase(
			sessionRepository = sessionRepository,
			messagingRepository = pushGateway,
			applicationRepository = applicationRepository,
			dependenciesRepository = dependenciesRepository
		)

		assertEquals(
			LoginDestination.NavGraph,
			startUpUseCase.startDestination()
		)

		val setDestinationStates = setLastDestinationUseCase.execute(LoginDestination.UpdatePasswordDialog)
			.toList()
		assertEquals(2, setDestinationStates.size)
		assertIs<UseCaseState.Data<Unit, Nothing>>(setDestinationStates.last())

		val signInStates = signInUseCase.execute(
			SignInParams(usbId = "20-32000", password = "secret")
		).toList()
		assertEquals(2, signInStates.size)
		assertIs<UseCaseState.Data<Unit, SignInUseCaseError>>(signInStates.last())

		assertEquals("uid-1", loginReportingRepository.lastIdentifier)
		assertEquals(listOf("push-token-kmp020"), messagingApiRepository.subscribedTokens)
		assertEquals(
			IssueTokensAttestationPayload(usbId = "20-32000", password = "secret"),
			attestationGateway.lastPayload
		)
		assertEquals("20-32000", sessionRepository.usbId)
		assertEquals("access-token", sessionRepository.accessToken)
		assertEquals("refresh-token", sessionRepository.refreshToken)
		assertEquals(1, authApiRepository.issueCalls)

		assertEquals(
			LoginDestination.UpdatePasswordDialog,
			startUpUseCase.startDestination()
		)

		val signOutStates = signOutUseCase.execute(Unit).toList()
		assertEquals(2, signOutStates.size)
		assertIs<UseCaseState.Data<Unit, Nothing>>(signOutStates.last())
		assertEquals(1, pushGateway.unsubscribeCalls)
		assertEquals(1, applicationRepository.clearDataCalls)
		assertEquals(1, dependenciesRepository.restartCalls)
		assertEquals(1, sessionRepository.clearCalls)
		assertTrue(sessionRepository.accessToken.isBlank())
		assertTrue(sessionRepository.refreshToken.isBlank())

		assertEquals(LoginDestination.NavGraph, startUpUseCase.startDestination())
	}

	@Test
	fun signIn_failure_keepsStartupOnLoginAndSkipsSideEffects() = runBlocking {
		val sessionRepository = WorkflowSessionRepository()
		val settingsRepository = WorkflowSettingsRepository(LoginDestination.UpdatePasswordDialog)
		val startUpUseCase = StartUpUseCase(
			sessionRepository = sessionRepository,
			settingsRepository = settingsRepository,
			configRepository = WorkflowConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = WorkflowApplicationRepository(),
				reportingRepository = WorkflowReportingGateway()
			)
		)
		val authApiRepository = WorkflowAuthApiRepository(shouldFailIssue = true)
		val messagingApiRepository = WorkflowMessagingApiRepository()
		val loginReportingRepository = WorkflowLoginReportingRepository()
		val signInUseCase = SignInUseCase(
			sessionRepository = sessionRepository,
			authApiRepository = authApiRepository,
			attestationRepository = WorkflowIntegrityGateway(),
			messagingApiRepository = messagingApiRepository,
			reportingRepository = loginReportingRepository,
			messagingRepository = WorkflowMessagingRepository(token = "push-token-kmp020"),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = WorkflowNetworkStatusGateway(),
				reportingRepository = WorkflowReportingGateway()
			)
		)

		val states = signInUseCase.execute(
			SignInParams(usbId = "20-32000", password = "secret")
		).toList()
		assertEquals(2, states.size)
		val failure = assertIs<UseCaseState.Error<Unit, SignInUseCaseError>>(states.last())
		assertNull(failure.error)
		assertEquals(1, authApiRepository.issueCalls)
		assertTrue(messagingApiRepository.subscribedTokens.isEmpty())
		assertNull(loginReportingRepository.lastIdentifier)
		assertTrue(sessionRepository.accessToken.isBlank())
		assertTrue(sessionRepository.refreshToken.isBlank())

		assertEquals(LoginDestination.NavGraph, startUpUseCase.startDestination())
	}

	@Test
	fun updatePassword_success_keepsSessionActiveAndPreservesStartupDestination() = runBlocking {
		val sessionRepository = WorkflowSessionRepository().apply {
			usbId = "20-32000"
			accessToken = "old-access"
			refreshToken = "old-refresh"
		}
		val settingsRepository = WorkflowSettingsRepository(LoginDestination.UpdatePasswordDialog)
		val authApiRepository = WorkflowAuthApiRepository()
		val attestationGateway = WorkflowIntegrityGateway()
		val startUpUseCase = StartUpUseCase(
			sessionRepository = sessionRepository,
			settingsRepository = settingsRepository,
			configRepository = WorkflowConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = WorkflowApplicationRepository(),
				reportingRepository = WorkflowReportingGateway()
			)
		)
		val updatePasswordUseCase = UpdatePasswordUseCase(
			authApiRepository = authApiRepository,
			sessionRepository = sessionRepository,
			attestationRepository = attestationGateway,
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = WorkflowNetworkStatusGateway(),
				reportingRepository = WorkflowReportingGateway()
			)
		)

		val updateStates = updatePasswordUseCase.execute("new-secret").toList()
		assertEquals(2, updateStates.size)
		assertIs<UseCaseState.Data<Unit, SignInUseCaseError>>(updateStates.last())
		assertEquals(1, authApiRepository.issueCalls)
		assertEquals(
			IssueTokensAttestationPayload(usbId = "20-32000", password = "new-secret"),
			attestationGateway.lastPayload
		)
		assertEquals("access-token", sessionRepository.accessToken)
		assertEquals("refresh-token", sessionRepository.refreshToken)

		assertEquals(
			LoginDestination.UpdatePasswordDialog,
			startUpUseCase.startDestination()
		)
	}

	@Test
	fun updatePassword_validationError_keepsPreviousSessionTokens() = runBlocking {
		val sessionRepository = WorkflowSessionRepository().apply {
			usbId = "20-32000"
			accessToken = "old-access"
			refreshToken = "old-refresh"
		}
		val settingsRepository = WorkflowSettingsRepository(LoginDestination.UpdatePasswordDialog)
		val authApiRepository = WorkflowAuthApiRepository()
		val startUpUseCase = StartUpUseCase(
			sessionRepository = sessionRepository,
			settingsRepository = settingsRepository,
			configRepository = WorkflowConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = WorkflowApplicationRepository(),
				reportingRepository = WorkflowReportingGateway()
			)
		)
		val updatePasswordUseCase = UpdatePasswordUseCase(
			authApiRepository = authApiRepository,
			sessionRepository = sessionRepository,
			attestationRepository = WorkflowIntegrityGateway(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = WorkflowNetworkStatusGateway(),
				reportingRepository = WorkflowReportingGateway()
			)
		)

		val updateStates = updatePasswordUseCase.execute("").toList()
		assertEquals(2, updateStates.size)
		val failure = assertIs<UseCaseState.Error<Unit, SignInUseCaseError>>(updateStates.last())
		assertEquals(SignInUseCaseError.EmptyPassword, failure.error)
		assertEquals(0, authApiRepository.issueCalls)
		assertEquals("old-access", sessionRepository.accessToken)
		assertEquals("old-refresh", sessionRepository.refreshToken)

		assertEquals(
			LoginDestination.UpdatePasswordDialog,
			startUpUseCase.startDestination()
		)
	}

	private suspend fun StartUpUseCase.startDestination(): Destination {
		val states = execute(Unit).toList()
		val data = assertIs<UseCaseState.Data<StartUpResult, StartUpUseCaseError>>(states.last())
		return data.value.startDestination
	}
}

private class WorkflowSessionRepository : SessionRepository {
	var usbId: String = ""
	var accessToken: String = ""
	var refreshToken: String = ""
	var clearCalls: Int = 0

	override suspend fun hasActiveSession(): Boolean {
		return accessToken.isNotBlank() || refreshToken.isNotBlank()
	}

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
		clearCalls++
		usbId = ""
		accessToken = ""
		refreshToken = ""
	}
}

private class WorkflowSettingsRepository(
	private var lastDestination: Destination
) : SettingsRepository {
	override suspend fun isReviewSuggested(value: Int): Boolean = false

	override suspend fun getLastDestination(): Destination = lastDestination

	override suspend fun setLastDestination(destination: Destination) {
		lastDestination = destination
	}

	override suspend fun clear() = Unit
}

private class WorkflowConfigGateway : ConfigGateway {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support"

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3
}

private class WorkflowAuthApiRepository(
	private val shouldFailIssue: Boolean = false
) : AuthApiRepository {
	var issueCalls: Int = 0

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		issueCalls++
		if (shouldFailIssue) error("issue-tokens-failed")

		return IssueTokens(
			uid = "uid-1",
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
	): RefreshTokens {
		return RefreshTokens(
			accessToken = accessToken,
			refreshToken = refreshToken,
			expiresIn = 3_600L
		)
	}

	override suspend fun revokeTokens() = Unit
}

private class WorkflowIntegrityGateway : IntegrityGateway {
	var lastPayload: IssueTokensAttestationPayload? = null

	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		lastPayload = payload as IssueTokensAttestationPayload
		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.APP_ATTEST,
			keyId = "attestation-key-id"
		)
	}
}

private class WorkflowMessagingRepository(
	private val token: String
) : MessagingRepository {
	override suspend fun getToken(): String = token
}

private class WorkflowMessagingApiRepository : MessagingApiRepository {
	val subscribedTokens = mutableListOf<String>()

	override suspend fun subscribe(token: String) {
		subscribedTokens += token
	}
}

private class WorkflowLoginReportingRepository : LoginReportingRepository {
	var lastIdentifier: String? = null

	override suspend fun setIdentifier(id: String) {
		lastIdentifier = id
	}
}

private class WorkflowPushGateway : PushGateway {
	var unsubscribeCalls: Int = 0

	override suspend fun subscribe() = Unit

	override suspend fun unsubscribe() {
		unsubscribeCalls++
	}
}

private class WorkflowApplicationRepository : ApplicationRepository {
	var clearDataCalls: Int = 0

	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = false

	override suspend fun clearData() {
		clearDataCalls++
	}
}

private class WorkflowDependenciesRepository : DependenciesRepository {
	var restartCalls: Int = 0

	override fun restart() {
		restartCalls++
	}
}

private class WorkflowNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class WorkflowReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
