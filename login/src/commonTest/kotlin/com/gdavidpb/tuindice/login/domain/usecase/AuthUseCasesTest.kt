package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.IssueTokensAttestationPayload
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository as BaseReportingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthUseCasesTest {
	@Test
	fun signIn_savesTokensAndSubscribesPushWithAttestation() = runBlocking {
		val sessionRepository = AuthUseCaseFakeSessionRepository()
		val authApiRepository = AuthUseCaseFakeAuthApiRepository()
		val attestationGateway = AuthUseCaseFakeIntegrityGateway()
		val messagingApiRepository = AuthUseCaseFakeMessagingApiRepository()
		val reportingRepository = AuthUseCaseFakeReportingRepository()
		val messagingRepository = AuthUseCaseFakeMessagingRepository(token = "push-token-123")
		val useCase = SignInUseCase(
			sessionRepository = sessionRepository,
			authApiRepository = authApiRepository,
			attestationRepository = attestationGateway,
			messagingApiRepository = messagingApiRepository,
			reportingRepository = reportingRepository,
			messagingRepository = messagingRepository,
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = AuthUseCaseFakeNetworkStatusGateway(),
				reportingRepository = AuthUseCaseFakeReportingGateway()
			)
		)

		val states = useCase.execute(SignInParams(usbId = "20-32000", password = "secret")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SignInUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, SignInUseCaseError>>(states[1])
		assertEquals("20-32000", authApiRepository.lastIssueUsbId)
		assertEquals("secret", authApiRepository.lastIssuePassword)
		assertEquals(
			IssueTokensAttestationPayload(usbId = "20-32000", password = "secret"),
			attestationGateway.lastPayload
		)
		assertEquals("20-32000", sessionRepository.usbId)
		assertEquals("access-token", sessionRepository.accessToken)
		assertEquals("refresh-token", sessionRepository.refreshToken)
		assertEquals("uid-1", reportingRepository.lastIdentifier)
		assertEquals("push-token-123", messagingApiRepository.lastSubscribedToken)
	}

	@Test
	fun signIn_whenUsbIdIsInvalid_emitsValidationErrorAndSkipsApiCalls() = runBlocking {
		val authApiRepository = AuthUseCaseFakeAuthApiRepository()
		val useCase = SignInUseCase(
			sessionRepository = AuthUseCaseFakeSessionRepository(),
			authApiRepository = authApiRepository,
			attestationRepository = AuthUseCaseFakeIntegrityGateway(),
			messagingApiRepository = AuthUseCaseFakeMessagingApiRepository(),
			reportingRepository = AuthUseCaseFakeReportingRepository(),
			messagingRepository = AuthUseCaseFakeMessagingRepository(token = "push-token-123"),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = AuthUseCaseFakeNetworkStatusGateway(),
				reportingRepository = AuthUseCaseFakeReportingGateway()
			)
		)

		val states = useCase.execute(SignInParams(usbId = "20320000", password = "secret")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SignInUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<Unit, SignInUseCaseError>>(states[1])
		assertEquals(SignInUseCaseError.InvalidUsbId, failure.error)
		assertNull(authApiRepository.lastIssueUsbId)
	}

	@Test
	fun signIn_whenMessagingTokenIsBlank_emitsErrorAndSkipsMessagingSubscription() = runBlocking {
		val messagingApiRepository = AuthUseCaseFakeMessagingApiRepository()
		val useCase = SignInUseCase(
			sessionRepository = AuthUseCaseFakeSessionRepository(),
			authApiRepository = AuthUseCaseFakeAuthApiRepository(),
			attestationRepository = AuthUseCaseFakeIntegrityGateway(),
			messagingApiRepository = messagingApiRepository,
			reportingRepository = AuthUseCaseFakeReportingRepository(),
			messagingRepository = AuthUseCaseFakeMessagingRepository(token = "   "),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = AuthUseCaseFakeNetworkStatusGateway(),
				reportingRepository = AuthUseCaseFakeReportingGateway()
			)
		)

		val states = useCase.execute(SignInParams(usbId = "20-32000", password = "secret")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SignInUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<Unit, SignInUseCaseError>>(states[1])
		assertNull(failure.error)
		assertNull(messagingApiRepository.lastSubscribedToken)
	}

	@Test
	fun updatePassword_usesStoredUsbIdAndReplacesSessionTokens() = runBlocking {
		val sessionRepository = AuthUseCaseFakeSessionRepository(
			usbId = "20-32000",
			accessToken = "old-access",
			refreshToken = "old-refresh"
		)
		val authApiRepository = AuthUseCaseFakeAuthApiRepository()
		val attestationGateway = AuthUseCaseFakeIntegrityGateway()
		val useCase = UpdatePasswordUseCase(
			authApiRepository = authApiRepository,
			sessionRepository = sessionRepository,
			attestationRepository = attestationGateway,
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = AuthUseCaseFakeNetworkStatusGateway(),
				reportingRepository = AuthUseCaseFakeReportingGateway()
			)
		)

		val states = useCase.execute("new-password").toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SignInUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, SignInUseCaseError>>(states[1])
		assertEquals("20-32000", authApiRepository.lastIssueUsbId)
		assertEquals("new-password", authApiRepository.lastIssuePassword)
		assertEquals(
			IssueTokensAttestationPayload(usbId = "20-32000", password = "new-password"),
			attestationGateway.lastPayload
		)
		assertEquals("access-token", sessionRepository.accessToken)
		assertEquals("refresh-token", sessionRepository.refreshToken)
	}

	@Test
	fun updatePassword_whenPasswordIsEmpty_emitsValidationError() = runBlocking {
		val authApiRepository = AuthUseCaseFakeAuthApiRepository()
		val useCase = UpdatePasswordUseCase(
			authApiRepository = authApiRepository,
			sessionRepository = AuthUseCaseFakeSessionRepository(usbId = "20-32000"),
			attestationRepository = AuthUseCaseFakeIntegrityGateway(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = AuthUseCaseFakeNetworkStatusGateway(),
				reportingRepository = AuthUseCaseFakeReportingGateway()
			)
		)

		val states = useCase.execute("").toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SignInUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<Unit, SignInUseCaseError>>(states[1])
		assertEquals(SignInUseCaseError.EmptyPassword, failure.error)
		assertNull(authApiRepository.lastIssuePassword)
	}
}

private class AuthUseCaseFakeSessionRepository(
	var usbId: String = "",
	var accessToken: String = "",
	var refreshToken: String = ""
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean {
		return accessToken.isNotEmpty() || refreshToken.isNotEmpty()
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
		usbId = ""
		accessToken = ""
		refreshToken = ""
	}
}

private class AuthUseCaseFakeIntegrityGateway : AttestationRepository {
	var lastPayload: IssueTokensAttestationPayload? = null

	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		lastPayload = payload as IssueTokensAttestationPayload

		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}

private class AuthUseCaseFakeAuthApiRepository : AuthApiRepository {
	var lastIssueUsbId: String? = null
	var lastIssuePassword: String? = null

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		lastIssueUsbId = usbId
		lastIssuePassword = password

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

private class AuthUseCaseFakeMessagingRepository(
	private val token: String
) : MessagingRepository {
	override suspend fun getToken(): String = token
}

private class AuthUseCaseFakeMessagingApiRepository : MessagingApiRepository {
	var lastSubscribedToken: String? = null

	override suspend fun subscribe(token: String) {
		lastSubscribedToken = token
	}
}

private class AuthUseCaseFakeReportingRepository : LoginReportingRepository {
	var lastIdentifier: String? = null

	override suspend fun setIdentifier(id: String) {
		lastIdentifier = id
	}
}

private class AuthUseCaseFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class AuthUseCaseFakeReportingGateway : BaseReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
