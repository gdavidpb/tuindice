package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
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

class SignOutViewModelTest {
	@Test
	fun signOutAction_whenUseCaseSucceeds_emitsNavigateToSignIn() = runBlocking {
		val viewModel = SignOutViewModel(
			signOutActionProcessor = SignOutActionProcessor(
				signOutUseCase = SignOutUseCase(
					sessionRepository = SignOutViewModelFakeSessionRepository(),
					messagingRepository = SignOutViewModelFakePushGateway(),
					applicationRepository = SignOutViewModelFakeApplicationRepository(),
					dependenciesRepository = SignOutViewModelFakeDependenciesRepository()
				),
				textProvider = SignOutViewModelFakeLoginTextProvider
			)
		)
		val effects = mutableListOf<SignOut.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.signOutAction()

			waitUntil { effects.isNotEmpty() }
			assertEquals(SignOut.State.LoggingOut, viewModel.state.value)
			assertEquals(SignOut.Effect.NavigateToSignIn, effects.single())
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun signOutAction_whenUseCaseFails_returnsIdleAndShowsErrorEffect() = runBlocking {
		val viewModel = SignOutViewModel(
			signOutActionProcessor = SignOutActionProcessor(
				signOutUseCase = SignOutUseCase(
					sessionRepository = SignOutViewModelFakeSessionRepository(),
					messagingRepository = SignOutViewModelFakePushGateway(
						unsubscribeThrowable = IllegalStateException("unsubscribe failed")
					),
					applicationRepository = SignOutViewModelFakeApplicationRepository(),
					dependenciesRepository = SignOutViewModelFakeDependenciesRepository()
				),
				textProvider = SignOutViewModelFakeLoginTextProvider
			)
		)
		val effects = mutableListOf<SignOut.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.signOutAction()

			waitUntil { effects.isNotEmpty() && viewModel.state.value == SignOut.State.Idle }
			val snackBar = assertIs<SignOut.Effect.ShowSnackBar>(effects.single())
			assertEquals("Default error", snackBar.message)
			assertEquals(SignOut.State.Idle, viewModel.state.value)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
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

private object SignOutViewModelFakeLoginTextProvider : LoginTextProvider {
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

private class SignOutViewModelFakeSessionRepository : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = true
	override suspend fun setUsbId(usbId: String) = Unit
	override suspend fun setAccessToken(accessToken: String) = Unit
	override suspend fun setRefreshToken(refreshToken: String) = Unit
	override suspend fun getUsbId(): String = "20320000"
	override suspend fun getAccessToken(): String = "access"
	override suspend fun getRefreshToken(): String = "refresh"
	override suspend fun clear() = Unit
}

private class SignOutViewModelFakePushGateway(
	private val unsubscribeThrowable: Throwable? = null
) : PushGateway {
	override suspend fun subscribe() = Unit
	override suspend fun unsubscribe() {
		unsubscribeThrowable?.let { throw it }
	}
}

private class SignOutViewModelFakeApplicationRepository : ApplicationRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true

	override suspend fun clearData() = Unit
}

private class SignOutViewModelFakeDependenciesRepository : DependenciesRepository {
	override fun restart() = Unit
}
