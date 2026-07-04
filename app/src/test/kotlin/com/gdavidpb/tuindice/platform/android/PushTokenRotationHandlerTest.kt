package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.domain.usecase.EnsureMessagingSubscribedUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushTokenRotationHandlerTest {
	@Test
	fun onPushTokenRotated_reSubscribes_whenSessionIsActive() = runTest {
		val messagingRepository = FakeMessagingRepository()
		val handler = createHandler(
			hasActiveSession = true,
			messagingRepository = messagingRepository
		)

		handler.onPushTokenRotated()
		advanceUntilIdle()

		assertEquals(1, messagingRepository.subscribeCalls)
	}

	@Test
	fun onPushTokenRotated_skipsSubscription_whenSessionIsMissing() = runTest {
		val messagingRepository = FakeMessagingRepository()
		val handler = createHandler(
			hasActiveSession = false,
			messagingRepository = messagingRepository
		)

		handler.onPushTokenRotated()
		advanceUntilIdle()

		assertEquals(0, messagingRepository.subscribeCalls)
	}

	@Test
	fun onPushTokenRotated_reportsNonFatal_whenSubscriptionFails() = runTest {
		val subscribeThrowable = IllegalStateException("Push token unavailable.")
		val messagingRepository = FakeMessagingRepository(throwable = subscribeThrowable)
		val reportingRepository = RecordingReportingRepository()
		val handler = createHandler(
			hasActiveSession = true,
			messagingRepository = messagingRepository,
			reportingRepository = reportingRepository
		)

		handler.onPushTokenRotated()
		advanceUntilIdle()

		assertEquals(1, messagingRepository.subscribeCalls)
		assertEquals(listOf<Throwable>(subscribeThrowable), reportingRepository.loggedExceptions)
	}

	private fun TestScope.createHandler(
		hasActiveSession: Boolean,
		messagingRepository: MessagingRepository,
		reportingRepository: ReportingRepository = RecordingReportingRepository()
	): PushTokenRotationHandler {
		val dispatcher = StandardTestDispatcher(testScheduler)

		return PushTokenRotationHandler(
			ensureMessagingSubscribedUseCase = EnsureMessagingSubscribedUseCase(
				sessionRepository = FakeSessionRepository(hasActiveSession = hasActiveSession),
				messagingRepository = messagingRepository,
				reportingRepository = reportingRepository
			),
			sessionCoroutineScope = SessionCoroutineScope(
				dispatchers = TestDispatchers(dispatcher = dispatcher)
			)
		)
	}
}

private class TestDispatchers(
	dispatcher: CoroutineDispatcher
) : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = dispatcher
	override val default: CoroutineDispatcher = dispatcher
	override val io: CoroutineDispatcher = dispatcher
	override val unconfined: CoroutineDispatcher = dispatcher
}

private class FakeSessionRepository(
	private val hasActiveSession: Boolean
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = hasActiveSession

	override suspend fun getActiveSessionSnapshot(): SessionSnapshot? = null

	override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) = Unit

	override suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean = false

	override suspend fun setUsbId(usbId: String) = Unit

	override suspend fun setSessionId(sessionId: String) = Unit

	override suspend fun setAccessToken(accessToken: String) = Unit

	override suspend fun setRefreshToken(refreshToken: String) = Unit

	override suspend fun getUsbId(): String = ""

	override suspend fun getSessionId(): String = ""

	override suspend fun getAccessToken(): String = ""

	override suspend fun getRefreshToken(): String = ""

	override suspend fun clear() = Unit
}

private class FakeMessagingRepository(
	private val throwable: Throwable? = null
) : MessagingRepository {
	var subscribeCalls = 0

	override suspend fun subscribe() {
		subscribeCalls++

		throwable?.let { throw it }
	}

	override suspend fun unsubscribe() = Unit
}

private class RecordingReportingRepository : ReportingRepository {
	val loggedExceptions = mutableListOf<Throwable>()

	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) {
		loggedExceptions += throwable
	}

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
