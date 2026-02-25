package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignOutUseCaseTest {
	@Test
	fun execute_unsubscribesAndClearsSessionAndRestartsDependencies() {
		runBlocking {
			val calls = mutableListOf<String>()
			val useCase = SignOutUseCase(
				sessionRepository = FakeSessionRepository(calls),
				messagingRepository = FakePushGateway(calls),
				applicationRepository = FakeApplicationRepository(calls),
				dependenciesRepository = FakeDependenciesRepository(calls)
			)

			val states = useCase.execute(Unit).toList()

			assertEquals(
				expected = listOf("unsubscribe", "clearSession", "clearData", "restartDependencies"),
				actual = calls
			)
			assertEquals(2, states.size)
			assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
			assertIs<UseCaseState.Data<Unit, Nothing>>(states[1])
		}
	}
}

private class FakeSessionRepository(
	private val calls: MutableList<String>
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = true

	override suspend fun setUsbId(usbId: String) = Unit

	override suspend fun setAccessToken(accessToken: String) = Unit

	override suspend fun setRefreshToken(refreshToken: String) = Unit

	override suspend fun getUsbId(): String = "20320000"

	override suspend fun getAccessToken(): String = "access"

	override suspend fun getRefreshToken(): String = "refresh"

	override suspend fun clear() {
		calls += "clearSession"
	}
}

private class FakePushGateway(
	private val calls: MutableList<String>
) : PushGateway {
	override suspend fun subscribe() = Unit

	override suspend fun unsubscribe() {
		calls += "unsubscribe"
	}
}

private class FakeApplicationRepository(
	private val calls: MutableList<String>
) : ApplicationRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef(nameHint)
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = false

	override suspend fun clearData() {
		calls += "clearData"
	}
}

private class FakeDependenciesRepository(
	private val calls: MutableList<String>
) : DependenciesRepository {
	override fun restart() {
		calls += "restartDependencies"
	}
}
