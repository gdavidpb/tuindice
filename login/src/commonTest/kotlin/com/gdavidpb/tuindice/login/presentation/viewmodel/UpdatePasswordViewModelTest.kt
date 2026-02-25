package com.gdavidpb.tuindice.login.presentation.viewmodel

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
import com.gdavidpb.tuindice.login.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.CoroutineStart
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

class UpdatePasswordViewModelTest {
	@Test
	fun setPasswordAction_updatesIdleState() = runBlocking {
		val viewModel = createViewModel(authApiRepository = UpdatePasswordViewModelFakeAuthApiRepository())
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.setPasswordAction("new-password")

			waitUntil {
				val state = viewModel.state.value
				state is UpdatePassword.State.Idle && state.password == "new-password"
			}
			val idle = assertIs<UpdatePassword.State.Idle>(viewModel.state.value)
			assertEquals("new-password", idle.password)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun signInAction_whenUseCaseSucceeds_emitsSuccessEffectAndUpdatingState() = runBlocking {
		val sessionRepository = UpdatePasswordViewModelFakeSessionRepository()
		val viewModel = createViewModel(
			sessionRepository = sessionRepository,
			authApiRepository = UpdatePasswordViewModelFakeAuthApiRepository()
		)
		val effects = mutableListOf<UpdatePassword.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }
			waitUntil { viewModel.effect.subscriptionCount.value > 0 }

			viewModel.signInAction("new-password")

			waitUntil { effects.isNotEmpty() }
			val effect = assertIs<UpdatePassword.Effect.ShowSnackBar>(effects.single())
			assertEquals("Password updated", effect.message)
			assertEquals(UpdatePassword.State.Updating(password = "new-password"), viewModel.state.value)
			assertEquals("new-access-token", sessionRepository.accessToken)
			assertEquals("new-refresh-token", sessionRepository.refreshToken)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun signInAction_whenUseCaseFails_emitsErrorEffectAndReturnsIdleWithError() = runBlocking {
		val viewModel = createViewModel(
			authApiRepository = UpdatePasswordViewModelFakeAuthApiRepository(
				issueTokensThrowable = IllegalStateException("issue tokens failed")
			)
		)
		val effects = mutableListOf<UpdatePassword.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }
			waitUntil { viewModel.effect.subscriptionCount.value > 0 }

			viewModel.signInAction("new-password")

			waitUntil {
				val state = viewModel.state.value
				effects.isNotEmpty() && state is UpdatePassword.State.Idle && state.error != null
			}

			val snackBar = assertIs<UpdatePassword.Effect.ShowSnackBar>(effects.single())
			assertEquals("Default error", snackBar.message)
			val idle = assertIs<UpdatePassword.State.Idle>(viewModel.state.value)
			assertEquals("new-password", idle.password)
			assertEquals("Default error", idle.error)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(
		sessionRepository: UpdatePasswordViewModelFakeSessionRepository = UpdatePasswordViewModelFakeSessionRepository(),
		authApiRepository: AuthApiRepository
	): UpdatePasswordViewModel {
		return UpdatePasswordViewModel(
			setUpdatePasswordActionProcessor = SetUpdatePasswordActionProcessor(),
			updatePasswordActionProcessor = UpdatePasswordActionProcessor(
				updatePasswordUseCase = UpdatePasswordUseCase(
					authApiRepository = authApiRepository,
					sessionRepository = sessionRepository,
					attestationRepository = UpdatePasswordViewModelFakeIntegrityGateway(),
					paramsValidator = UpdatePasswordParamsValidator(),
					exceptionHandler = UpdatePasswordExceptionHandler(
						networkRepository = UpdatePasswordViewModelFakeNetworkStatusGateway(),
						reportingRepository = UpdatePasswordViewModelFakeReportingGateway()
					)
				),
				textProvider = UpdatePasswordViewModelFakeLoginTextProvider
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

private object UpdatePasswordViewModelFakeLoginTextProvider : LoginTextProvider {
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

private class UpdatePasswordViewModelFakeAuthApiRepository(
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

private class UpdatePasswordViewModelFakeSessionRepository : SessionRepository {
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

private class UpdatePasswordViewModelFakeIntegrityGateway : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		return Attestation(
			id = "attestation-id",
			token = "attestation-token",
			provider = AttestationProvider.APP_ATTEST,
			keyId = "attestation-key"
		)
	}
}

private class UpdatePasswordViewModelFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class UpdatePasswordViewModelFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
