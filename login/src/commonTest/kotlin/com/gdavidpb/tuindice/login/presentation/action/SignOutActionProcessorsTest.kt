package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignOutActionProcessorsTest {
	@Test
	fun confirmSignOutActionProcessor_whenUseCaseSucceeds_navigatesToSignIn() = runBlocking {
		val processor = SignOutActionProcessor(
			signOutUseCase = SignOutUseCase(
				sessionRepository = SignOutFakeSessionRepository(),
				messagingRepository = SignOutFakePushGateway(),
				applicationRepository = SignOutFakeApplicationRepository(),
				dependenciesRepository = SignOutFakeDependenciesRepository()
			),
			textProvider = SignOutFakeLoginTextProvider
		)
		val effects = mutableListOf<SignOut.Effect>()

		val mutations = processor.process(
			action = SignOut.Action.ConfirmSignOut,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(SignOut.State.Idle, mutations)

		assertEquals(SignOut.State.LoggingOut, finalState)
		assertEquals(SignOut.Effect.NavigateToSignIn, effects.single())
	}

	@Test
	fun confirmSignOutActionProcessor_whenUseCaseFails_returnsIdleAndShowsSnackBar() = runBlocking {
		val processor = SignOutActionProcessor(
			signOutUseCase = SignOutUseCase(
				sessionRepository = SignOutFakeSessionRepository(),
				messagingRepository = SignOutFakePushGateway(
					unsubscribeThrowable = IllegalStateException("unsubscribe failed")
				),
				applicationRepository = SignOutFakeApplicationRepository(),
				dependenciesRepository = SignOutFakeDependenciesRepository()
			),
			textProvider = SignOutFakeLoginTextProvider
		)
		val effects = mutableListOf<SignOut.Effect>()

		val mutations = processor.process(
			action = SignOut.Action.ConfirmSignOut,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(SignOut.State.Idle, mutations)

		assertEquals(SignOut.State.Idle, finalState)
		val snackBar = assertIs<SignOut.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	private fun applyMutations(
		initialState: SignOut.State,
		mutations: List<(SignOut.State) -> SignOut.State>
	): SignOut.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object SignOutFakeLoginTextProvider : LoginTextProvider {
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

private class SignOutFakeSessionRepository : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = true

	override suspend fun setUsbId(usbId: String) = Unit

	override suspend fun setAccessToken(accessToken: String) = Unit

	override suspend fun setRefreshToken(refreshToken: String) = Unit

	override suspend fun getUsbId(): String = "20320000"

	override suspend fun getAccessToken(): String = "access"

	override suspend fun getRefreshToken(): String = "refresh"

	override suspend fun clear() = Unit
}

private class SignOutFakePushGateway(
	private val unsubscribeThrowable: Throwable? = null
) : PushGateway {
	override suspend fun subscribe() = Unit

	override suspend fun unsubscribe() {
		unsubscribeThrowable?.let { throw it }
	}
}

private class SignOutFakeApplicationRepository : ApplicationRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true

	override suspend fun clearData() = Unit
}

private class SignOutFakeDependenciesRepository : DependenciesRepository {
	override fun restart() = Unit
}
