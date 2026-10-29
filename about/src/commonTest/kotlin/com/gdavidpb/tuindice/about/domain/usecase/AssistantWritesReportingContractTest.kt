package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// What the writes of the assistant report when their caller is gone before they end: a failure is reported once, and
// neither a success nor a write the session cancelled is reported as a failure.
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantWritesReportingContractTest {
	@Test
	fun setAssistantConsent_whenTheCallerLeavesAndTheWriteFails_reportsTheFailureOnce() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val reporting = RecordingReportingRepository()
		val useCase = SetAssistantConsentUseCase(
			assistantConsentRepository = FakeAssistantConsentRepository(writesFail = true, writeGate = writeGate),
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)
		// The caller runs in place, so it is suspended on the write before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute(true).collect() }

		// The caller leaves while the write waits for the store, and the store then refuses the write.
		caller.cancel()
		writeGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(
			listOf("The assistant consent store is unavailable"),
			reporting.loggedExceptions.map { it.message }
		)
	}

	@Test
	fun setAssistantConsent_whenTheCallerStaysAndTheWriteFails_reportsTheFailureOnce() = runTest {
		val reporting = RecordingReportingRepository()
		val useCase = SetAssistantConsentUseCase(
			assistantConsentRepository = FakeAssistantConsentRepository(writesFail = true),
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)

		useCase.execute(true).collect()

		assertEquals(1, reporting.loggedExceptions.size)
	}

	@Test
	fun setAssistantConsent_whenTheCallerLeavesAndTheWriteEnds_reportsNothing() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val assistantConsent = FakeAssistantConsentRepository(writeGate = writeGate)
		val reporting = RecordingReportingRepository()
		val useCase = SetAssistantConsentUseCase(
			assistantConsentRepository = assistantConsent,
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)
		// The caller runs in place, so it is suspended on the write before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute(true).collect() }

		caller.cancel()
		writeGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConsent.writes)
		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun setAssistantConsent_whenTheSessionEndsTheWrite_reportsNothing() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val reporting = RecordingReportingRepository()
		val useCase = SetAssistantConsentUseCase(
			assistantConsentRepository = FakeAssistantConsentRepository(writeGate = writeGate),
			sessionCoroutineScope = session,
			reportingRepository = reporting
		)
		backgroundScope.launch { useCase.execute(true).collect() }
		testScheduler.advanceUntilIdle()

		// The session ends while the write waits: the write is cancelled, and a cancelled write is not a failure.
		session.cancelActiveWork()
		testScheduler.advanceUntilIdle()

		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun deleteAssistantConversations_whenTheCallerLeavesAndTheDeletionFails_reportsTheFailureOnce() = runTest {
		val deleteGate = CompletableDeferred<Unit>()
		val conversations = FakeAssistantConversationsRepository(deletesFail = true, deleteGate = deleteGate)
		val reporting = RecordingReportingRepository()
		val useCase = DeleteAssistantConversationsUseCase(
			assistantConversationsRepository = conversations,
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)
		// The caller runs in place, so it is suspended on the deletion before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute(Unit).collect() }

		// The caller leaves while the deletion waits for the store, and the store then refuses it.
		caller.cancel()
		deleteGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(
			listOf("The assistant conversations are unavailable"),
			reporting.loggedExceptions.map { it.message }
		)
	}

	@Test
	fun deleteAssistantConversations_whenTheCallerLeavesAndTheDeletionEnds_reportsNothing() = runTest {
		val deleteGate = CompletableDeferred<Unit>()
		val conversations = FakeAssistantConversationsRepository(deleteGate = deleteGate)
		val reporting = RecordingReportingRepository()
		val useCase = DeleteAssistantConversationsUseCase(
			assistantConversationsRepository = conversations,
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)
		// The caller runs in place, so it is suspended on the deletion before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute(Unit).collect() }

		caller.cancel()
		deleteGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(1, conversations.deletions)
		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun deleteAssistantConversations_whenTheSessionEndsTheDeletion_reportsNothing() = runTest {
		val deleteGate = CompletableDeferred<Unit>()
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val reporting = RecordingReportingRepository()
		val useCase = DeleteAssistantConversationsUseCase(
			assistantConversationsRepository = FakeAssistantConversationsRepository(deleteGate = deleteGate),
			sessionCoroutineScope = session,
			reportingRepository = reporting
		)
		backgroundScope.launch { useCase.execute(Unit).collect() }
		testScheduler.advanceUntilIdle()

		// The session ends while the deletion waits: the deletion is cancelled, and a cancelled deletion is not a failure.
		session.cancelActiveWork()
		testScheduler.advanceUntilIdle()

		assertTrue(reporting.loggedExceptions.isEmpty())
	}
}
