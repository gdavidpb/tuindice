package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationStoreDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantIntentOutboxDataSource
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// A queued mutation settles after its session ended: the follow-up writes nothing and sends nothing for the account
// that left. The stack is the real one (store, outbox and conversation), so the session mark really changes.
class AssistantQueuedFollowAfterSessionTest {
	private val calls = mutableListOf<String>()
	private val store = AssistantConversationDataSource()
	private val conversation = AssistantConversationStoreDataSource(store)
	private val outbox = AssistantIntentOutboxDataSource(store)
	private val intents = RecordingAssistantIntentRepository(calls)
	private val evaluations = RecordingAssistantEvaluationsRepository(calls)
	private val queued = RecordingAssistantQueuedMutationsRepository(calls)
	private val reporting = RecordingReportingRepository()
	private val confirmer = AssistantIntentConfirmer(
		conversationRepository = conversation,
		intentRepository = intents,
		clock = FixedClock(nowMillis = NOW_MILLIS),
		outboxRepository = outbox,
		queuedMutationsRepository = queued
	)

	// The student confirms an edit, and the server takes it as queued: the mutation is followed past that answer.
	private suspend fun confirmQueuedEdit() {
		val intent = assistantIntent(
			args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0),
			mutationIds = listOf("m1")
		)
		store.addAssistantMessage(
			message = AssistantMessage(id = "m0", parts = listOf(Text("Confirma")), createdAtMillis = 1L),
			intent = intent,
			sessionMark = store.sessionMark()
		)
		evaluations.answer = IntentExecution(outcome = OutcomeKind.QUEUED)

		ConfirmEditEvaluationIntentUseCase(
			confirmer = confirmer,
			evaluationsRepository = evaluations,
			reportingRepository = reporting,
			exceptionHandler = AssistantIntentExceptionHandler()
		).execute(ConfirmIntentParams(intentId = "i1", editedArgs = null)).lastState()
	}

	@Test
	fun aQueuedMutationSettledAfterTheSessionEndedSendsNothingAndStoresNothing() = runTest {
		confirmQueuedEdit()
		assertEquals(listOf("m1"), queued.followed.single().first)
		assertEquals(1, intents.reports.size, "the queued answer itself was reported while the session ran")

		store.clearSessionMemory()
		queued.settle(listOf(MutationOutcome(mutationId = "m1", outcome = OutcomeKind.CONFIRMED)))

		assertEquals(1, intents.reports.size, "no outcome is reported for the account that left")
		assertTrue(outbox.pendingOutcomes().isEmpty(), "no outcome is staged for the account that follows")
		assertEquals(AssistantConversationState(sessionGeneration = 1), store.observeConversation().value)
	}

	// The same settlement during the session that started it is reported: the test above fails only for the session.
	@Test
	fun aQueuedMutationSettledDuringItsSessionIsReported() = runTest {
		confirmQueuedEdit()

		queued.settle(listOf(MutationOutcome(mutationId = "m1", outcome = OutcomeKind.CONFIRMED)))

		assertEquals(2, intents.reports.size)
		assertEquals(OutcomeKind.CONFIRMED, intents.reports.last().outcome)
		assertTrue(outbox.pendingOutcomes().isEmpty())
	}

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
