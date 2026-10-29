package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConsentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
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
// neither a success nor a write the session cancelled is reported as a failure. The writes are the consent, the
// dismissal of an intent and its confirmation; the about module's SetAssistantConsentUseCase writes its own the same
// way.
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantWritesReportingContractTest {
	@Test
	fun acceptConsent_whenTheCallerLeavesAndTheWriteFails_reportsTheFailureOnce() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val consent = RecordingAssistantConsentRepository().apply {
			gate = writeGate
			failure = IllegalStateException("The assistant consent store is unavailable")
		}
		val reporting = RecordingReportingRepository()
		val useCase = AcceptAssistantConsentUseCase(
			consentRepository = consent,
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
	fun acceptConsent_whenTheCallerStaysAndTheWriteFails_reportsTheFailureOnce() = runTest {
		val consent = RecordingAssistantConsentRepository().apply {
			failure = IllegalStateException("The assistant consent store is unavailable")
		}
		val reporting = RecordingReportingRepository()
		val useCase = AcceptAssistantConsentUseCase(
			consentRepository = consent,
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)

		useCase.execute(true).collect()

		assertEquals(1, reporting.loggedExceptions.size)
	}

	@Test
	fun acceptConsent_whenTheCallerLeavesAndTheWriteEnds_reportsNothing() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val consent = RecordingAssistantConsentRepository().apply { gate = writeGate }
		val reporting = RecordingReportingRepository()
		val useCase = AcceptAssistantConsentUseCase(
			consentRepository = consent,
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler)),
			reportingRepository = reporting
		)
		// The caller runs in place, so it is suspended on the write before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute(true).collect() }

		caller.cancel()
		writeGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(listOf(true), consent.changes)
		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun acceptConsent_whenTheSessionEndsTheWrite_reportsNothing() = runTest {
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val reporting = RecordingReportingRepository()
		val useCase = AcceptAssistantConsentUseCase(
			consentRepository = RecordingAssistantConsentRepository().apply { gate = CompletableDeferred() },
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
	fun dismissIntent_whenTheCallerLeavesAndTheDeclineFails_reportsTheFailureOnce() = runTest {
		val decisionGate = CompletableDeferred<Unit>()
		val intents = RecordingAssistantIntentRepository().apply {
			onDecide = {
				decisionGate.await()
				error("The server did not take the decline")
			}
		}
		val reporting = RecordingReportingRepository()
		val useCase = dismissUseCase(
			intents = intents,
			conversation = openRememberConversation(),
			reporting = reporting,
			session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		// The caller runs in place, so it is suspended on the decline before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute("i1").collect() }

		// The caller leaves while the decline waits for the server, and the server then refuses it.
		caller.cancel()
		decisionGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(
			listOf("The server did not take the decline"),
			reporting.loggedExceptions.map { it.message }
		)
	}

	@Test
	fun dismissIntent_whenTheCallerLeavesAndTheDeclineEnds_reportsNothing() = runTest {
		val decisionGate = CompletableDeferred<Unit>()
		val intents = RecordingAssistantIntentRepository().apply { onDecide = { decisionGate.await() } }
		val conversation = openRememberConversation()
		val reporting = RecordingReportingRepository()
		val useCase = dismissUseCase(
			intents = intents,
			conversation = conversation,
			reporting = reporting,
			session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.execute("i1").collect() }

		caller.cancel()
		decisionGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = false)), intents.decisions)
		assertEquals(mapOf("i1" to OutcomeKind.CANCELLED), conversation.resolved)
		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun dismissIntent_whenTheSessionEndsTheDecline_reportsNothing() = runTest {
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val reporting = RecordingReportingRepository()
		val useCase = dismissUseCase(
			intents = RecordingAssistantIntentRepository().apply { onDecide = { CompletableDeferred<Unit>().await() } },
			conversation = openRememberConversation(),
			reporting = reporting,
			session = session
		)
		backgroundScope.launch { useCase.execute("i1").collect() }
		testScheduler.advanceUntilIdle()

		// The session ends while the decline waits: the decline is cancelled, and a cancelled decline is not a failure.
		session.cancelActiveWork()
		testScheduler.advanceUntilIdle()

		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun confirmIntent_whenTheCallerLeavesAndTheDecisionFails_reportsTheFailureOnce() = runTest {
		val decisionGate = CompletableDeferred<Unit>()
		val intents = RecordingAssistantIntentRepository().apply {
			onDecide = {
				decisionGate.await()
				error("The server did not take the decision")
			}
		}
		val reporting = RecordingReportingRepository()
		val useCase = confirmRememberUseCase(
			intents = intents,
			conversation = openRememberConversation(),
			reporting = reporting,
			session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		// The caller runs in place, so it is suspended on the decision before the test goes on, and it leaves in place too.
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			useCase.execute(ConfirmIntentParams(intentId = "i1")).collect()
		}

		// The caller leaves while the decision waits for the server, and the server then refuses it.
		caller.cancel()
		decisionGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(
			listOf("The server did not take the decision"),
			reporting.loggedExceptions.map { it.message }
		)
	}

	@Test
	fun confirmIntent_whenTheCallerLeavesAndTheDecisionEnds_reportsNothing() = runTest {
		val decisionGate = CompletableDeferred<Unit>()
		val intents = RecordingAssistantIntentRepository().apply { onDecide = { decisionGate.await() } }
		val conversation = openRememberConversation()
		val reporting = RecordingReportingRepository()
		val useCase = confirmRememberUseCase(
			intents = intents,
			conversation = conversation,
			reporting = reporting,
			session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		val caller = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			useCase.execute(ConfirmIntentParams(intentId = "i1")).collect()
		}

		caller.cancel()
		decisionGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	@Test
	fun confirmIntent_whenTheSessionEndsTheDecision_reportsNothing() = runTest {
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val reporting = RecordingReportingRepository()
		val useCase = confirmRememberUseCase(
			intents = RecordingAssistantIntentRepository().apply { onDecide = { CompletableDeferred<Unit>().await() } },
			conversation = openRememberConversation(),
			reporting = reporting,
			session = session
		)
		backgroundScope.launch { useCase.execute(ConfirmIntentParams(intentId = "i1")).collect() }
		testScheduler.advanceUntilIdle()

		// The session ends while the decision waits: the confirmation is cancelled, and a cancelled one is not a failure.
		session.cancelActiveWork()
		testScheduler.advanceUntilIdle()

		assertTrue(reporting.loggedExceptions.isEmpty())
	}

	// A conversation with one open intent to remember, as the sheet shows it: the dismissal and the confirmation take it.
	private fun openRememberConversation(): RecordingAssistantConversationRepository {
		val args = RememberArgs(statement = "Estudia de noche", category = "habitos", quote = "estudio de noche")

		return RecordingAssistantConversationRepository().apply {
			openIntents = mapOf("i1" to assistantIntent(id = "i1", args = args, mutationIds = emptyList()))
		}
	}

	private fun dismissUseCase(
		intents: RecordingAssistantIntentRepository,
		conversation: RecordingAssistantConversationRepository,
		reporting: RecordingReportingRepository,
		session: SessionCoroutineScope
	) = DismissAssistantIntentUseCase(
		intentRepository = intents,
		conversationRepository = conversation,
		reportingRepository = reporting,
		outboxRepository = conversation,
		sessionCoroutineScope = session
	)

	private fun confirmRememberUseCase(
		intents: RecordingAssistantIntentRepository,
		conversation: RecordingAssistantConversationRepository,
		reporting: RecordingReportingRepository,
		session: SessionCoroutineScope
	) = ConfirmRememberIntentUseCase(
		confirmer = AssistantIntentConfirmer(
			conversationRepository = conversation,
			intentRepository = intents,
			outboxRepository = conversation,
			queuedMutationsRepository = RecordingAssistantQueuedMutationsRepository(),
			sessionCoroutineScope = session,
			reportingRepository = reporting
		),
		reportingRepository = reporting,
		exceptionHandler = AssistantIntentExceptionHandler()
	)
}
