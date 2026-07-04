package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakeMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnsureMessagingSubscribedUseCaseTest {
	@Test
	fun executeOnBackground_subscribes_whenSessionIsActive() = runTest {
		val messagingRepository = FakeMessagingRepository()
		val useCase = createUseCase(messagingRepository = messagingRepository)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(1, messagingRepository.subscribeCalls)
	}

	@Test
	fun executeOnBackground_doesNotSubscribe_whenSessionIsMissing() = runTest {
		val messagingRepository = FakeMessagingRepository()
		val useCase = createUseCase(
			sessionRepository = FakeSessionRepository(sessionId = ""),
			messagingRepository = messagingRepository
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(0, messagingRepository.subscribeCalls)
	}

	@Test
	fun execute_reportsNonFatalAndEmitsError_whenSubscriptionFails() = runTest {
		val subscribeThrowable = IllegalStateException("Push token unavailable.")
		val messagingRepository = FakeMessagingRepository(throwable = subscribeThrowable)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			messagingRepository = messagingRepository,
			reportingRepository = reportingRepository
		)

		val states = useCase.execute(Unit).toList()

		assertTrue(states.last() is UseCaseState.Error<*>)
		assertEquals(subscribeThrowable, reportingRepository.loggedExceptions.single())
		assertEquals("EnsureMessagingSubscribedUseCase", reportingRepository.customKeys["use-case"])
	}

	private fun createUseCase(
		sessionRepository: FakeSessionRepository = FakeSessionRepository(),
		messagingRepository: FakeMessagingRepository = FakeMessagingRepository(),
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	) = EnsureMessagingSubscribedUseCase(
		sessionRepository = sessionRepository,
		messagingRepository = messagingRepository,
		reportingRepository = reportingRepository
	)
}
