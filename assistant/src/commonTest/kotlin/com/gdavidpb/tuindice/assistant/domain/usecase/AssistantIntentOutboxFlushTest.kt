package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationStoreDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantIntentOutboxDataSource
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The outbox the confirmer flushes: the reports the server has not taken are sent again by the next flush.
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantIntentOutboxFlushTest {
	private val calls = mutableListOf<String>()
	private val conversation = RecordingAssistantConversationRepository(calls)
	private val intents = RecordingAssistantIntentRepository(calls)
	private val confirmer = AssistantIntentConfirmer(
		conversationRepository = conversation,
		intentRepository = intents,
		outboxRepository = conversation,
		queuedMutationsRepository = RecordingAssistantQueuedMutationsRepository(calls),
		sessionCoroutineScope = testSessionCoroutineScope(),
		reportingRepository = RecordingReportingRepository()
	)

	// A flush that is sending an older report when a newer one for the same intent is staged sends the newer one: the
	// older report does not replace it, and the newer one stays owed until the server takes it.
	@Test
	fun aFlushSendsTheOutcomeStagedNowNotTheOlderOneItStartedWith() = runTest {
		val mark = conversation.sessionMark()
		val older = IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.QUEUED)
		val newer = IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.CONFIRMED)
		val held = CompletableDeferred<Unit>()
		conversation.stageOutcome(IntentOutcomeReport(intentId = "j1", outcome = OutcomeKind.CONFIRMED), mark)
		conversation.stageOutcome(older, mark)
		// The flush holds the report of j1 until the newer one is staged, and the server refuses every report of i1.
		intents.onReport = { report -> if (report.intentId == "j1") held.await() else error("offline") }

		val flush = launch { confirmer.flushPendingOutcomes() }
		runCurrent()
		conversation.stageOutcome(newer, mark)
		held.complete(Unit)
		flush.join()

		assertEquals(listOf(newer), conversation.pendingOutcomes())

		intents.onReport = {}
		confirmer.flushPendingOutcomes()

		assertEquals(listOf(newer), intents.reports.filter { report -> report.intentId == "i1" })
		assertTrue(conversation.pendingOutcomes().isEmpty())
	}

	// Over the stack the app runs (store, outbox and conversation), not the double: the comparison that clears a settled
	// report is the outbox's. The older report's answer clears nothing of the newer one, which stays owed until a flush
	// sends it.
	@Test
	fun aSettledOlderReportDoesNotClearTheNewerOneStagedWhileItWasSent() = runTest {
		val store = AssistantConversationDataSource()
		val stackConversation = AssistantConversationStoreDataSource(store)
		val stackOutbox = AssistantIntentOutboxDataSource(store)
		val stackConfirmer = AssistantIntentConfirmer(
			conversationRepository = stackConversation,
			intentRepository = intents,
			outboxRepository = stackOutbox,
			queuedMutationsRepository = RecordingAssistantQueuedMutationsRepository(calls),
			sessionCoroutineScope = testSessionCoroutineScope(),
			reportingRepository = RecordingReportingRepository()
		)
		val mark = stackConversation.sessionMark()
		val older = IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.QUEUED)
		val newer = IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.CONFIRMED)
		val held = CompletableDeferred<Unit>()
		stackOutbox.stageOutcome(older, mark)
		// The server holds the older report until the newer one is staged, and refuses the newer one.
		intents.onReport = { report -> if (report.outcome == OutcomeKind.QUEUED) held.await() else error("offline") }

		val flush = launch { stackConfirmer.flushPendingOutcomes() }
		runCurrent()
		stackOutbox.stageOutcome(newer, mark)
		held.complete(Unit)
		flush.join()

		assertEquals(listOf(newer), stackOutbox.pendingOutcomes())

		intents.onReport = {}
		stackConfirmer.flushPendingOutcomes()

		assertEquals(listOf(older, newer), intents.reports)
		assertTrue(stackOutbox.pendingOutcomes().isEmpty())
	}
}
