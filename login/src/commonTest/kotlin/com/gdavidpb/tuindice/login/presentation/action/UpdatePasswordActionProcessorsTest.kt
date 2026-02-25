package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class UpdatePasswordActionProcessorsTest {
	@Test
	fun setUpdatePasswordActionProcessor_updatesIdlePassword() = runBlocking {
		val processor = SetUpdatePasswordActionProcessor()

		val mutations = processor.process(
			action = UpdatePassword.Action.SetPassword(password = "new-password"),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(
			initialState = UpdatePassword.State.Idle(password = "old-password"),
			mutations = mutations
		)

		val idle = assertIs<UpdatePassword.State.Idle>(finalState)
		assertEquals("new-password", idle.password)
		assertNull(idle.error)
	}

	@Test
	fun setUpdatePasswordActionProcessor_whenStateIsUpdating_keepsState() = runBlocking {
		val processor = SetUpdatePasswordActionProcessor()
		val updatingState = UpdatePassword.State.Updating(password = "same-password")

		val mutations = processor.process(
			action = UpdatePassword.Action.SetPassword(password = "ignored"),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(updatingState, mutations)

		assertEquals(updatingState, finalState)
	}

	@Test
	fun updatePasswordActionProcessor_whenUseCaseSucceeds_showsSuccessSnackBar() = runBlocking {
		val sessionRepository = UpdatePasswordFakeSessionRepository()
		val processor = UpdatePasswordActionProcessor(
			updatePasswordUseCase = createUpdatePasswordUseCase(
				sessionRepository = sessionRepository,
				authApiRepository = UpdatePasswordFakeAuthApiRepository()
			),
			textProvider = UpdatePasswordFakeLoginTextProvider
		)
		val effects = mutableListOf<UpdatePassword.Effect>()

		val mutations = processor.process(
			action = UpdatePassword.Action.ClickSignIn(password = "new-password"),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = UpdatePassword.State.Idle(password = "new-password"),
			mutations = mutations
		)

		assertEquals(UpdatePassword.State.Updating(password = "new-password"), finalState)
		val snackBar = assertIs<UpdatePassword.Effect.ShowSnackBar>(effects.single())
		assertEquals("Password updated", snackBar.message)
		assertEquals("new-access-token", sessionRepository.accessToken)
		assertEquals("new-refresh-token", sessionRepository.refreshToken)
	}

	@Test
	fun updatePasswordActionProcessor_whenUseCaseFails_returnsIdleWithError() = runBlocking {
		val sessionRepository = UpdatePasswordFakeSessionRepository()
		val processor = UpdatePasswordActionProcessor(
			updatePasswordUseCase = createUpdatePasswordUseCase(
				sessionRepository = sessionRepository,
				authApiRepository = UpdatePasswordFakeAuthApiRepository(
					issueTokensThrowable = IllegalStateException("issue tokens failed")
				)
			),
			textProvider = UpdatePasswordFakeLoginTextProvider
		)
		val effects = mutableListOf<UpdatePassword.Effect>()

		val mutations = processor.process(
			action = UpdatePassword.Action.ClickSignIn(password = "new-password"),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = UpdatePassword.State.Idle(password = "new-password"),
			mutations = mutations
		)

		val idle = assertIs<UpdatePassword.State.Idle>(finalState)
		assertEquals("new-password", idle.password)
		assertEquals("Default error", idle.error)
		val snackBar = assertIs<UpdatePassword.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
		assertNull(sessionRepository.accessToken)
		assertNull(sessionRepository.refreshToken)
	}

	private fun createUpdatePasswordUseCase(
		sessionRepository: UpdatePasswordFakeSessionRepository,
		authApiRepository: AuthApiRepository
	): UpdatePasswordUseCase {
		return UpdatePasswordUseCase(
			authApiRepository = authApiRepository,
			sessionRepository = sessionRepository,
			attestationRepository = UpdatePasswordFakeIntegrityGateway(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = UpdatePasswordFakeNetworkStatusGateway(),
				reportingRepository = UpdatePasswordFakeReportingGateway()
			)
		)
	}

	private fun applyMutations(
		initialState: UpdatePassword.State,
		mutations: List<(UpdatePassword.State) -> UpdatePassword.State>
	): UpdatePassword.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object UpdatePasswordFakeLoginTextProvider : LoginTextProvider {
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

private class UpdatePasswordFakeAuthApiRepository(
	private val issueTokensThrowable: Throwable? = null
) : AuthApiRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		issueTokensThrowable?.let { throw it }

		return IssueTokens(
			uid = "uid-1",
			usbId = usbId,
			accessToken = "new-access-token",
			refreshToken = "new-refresh-token",
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

private class UpdatePasswordFakeSessionRepository : SessionRepository {
	var accessToken: String? = null
	var refreshToken: String? = null

	override suspend fun hasActiveSession(): Boolean = true

	override suspend fun setUsbId(usbId: String) = Unit

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String = "20320000"

	override suspend fun getAccessToken(): String = accessToken.orEmpty()

	override suspend fun getRefreshToken(): String = refreshToken.orEmpty()

	override suspend fun clear() {
		accessToken = null
		refreshToken = null
	}
}

private class UpdatePasswordFakeIntegrityGateway : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.APP_ATTEST,
			keyId = "attestation-key"
		)
	}
}

private class UpdatePasswordFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class UpdatePasswordFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
