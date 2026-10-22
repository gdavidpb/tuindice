package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantConversation
import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.model.IntentResolvedCount
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentBinding
import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import com.gdavidpb.tuindice.chatcontract.intent.IntentStatus
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssistantConversationDataSourceTest {

	private val message = AssistantMessage(id = "m1", parts = listOf(Text("Hola")), createdAtMillis = 1L)

	private val intent = Intent(
		id = "i1",
		args = UnknownIntentArgs,
		title = "Agregar evaluacion",
		effect = listOf(IntentEffectLine(label = "Crear", detail = "Una evaluacion nueva")),
		risk = IntentRisk.NORMAL,
		binding = IntentBinding(mutationIds = emptyList(), argsHash = "hash"),
		expiresAtMillis = 1L,
		confirmLabel = "Confirmar",
		cancelLabel = "Cancelar"
	)

	@Test
	fun theItemsFollowTheOrderTheyHappened_notTheClocksThatStampedThem() = runTest {
		val store = AssistantConversationDataSource()
		// The device stamped the student's message later than the server stamped the answer, so the clocks disagree.
		store.addUserMessage(AssistantUserMessage(id = "u1", text = "Hola", createdAtMillis = 5_000L))
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(
			MessageCompleted(
				seq = 2,
				turnId = "t1",
				message = AssistantMessage(id = "m1", parts = listOf(Text("¿En qué te ayudo?")), createdAtMillis = 1_000L)
			),
			store.sessionMark()
		)

		val ids = store.observeConversation().value.toAssistantConversation().items.map { item ->
			when (item) {
				is AssistantConversationItem.User -> item.message.id
				is AssistantConversationItem.Agent -> item.message.id
			}
		}

		assertEquals(listOf("u1", "m1"), ids)
	}

	@Test
	fun aTurnWhoseResultIsUnknownIsUnconfirmed_andRunsAgainWhenItsNextEventComes() = runTest {
		val store = AssistantConversationDataSource()
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())

		store.markUnconfirmed(turnId = "t1", lastSeq = 1L, store.sessionMark())
		assertEquals(AssistantTurnStatus.UNCONFIRMED, store.observeConversation().value.turns.getValue("t1").status)

		store.apply(Progress(seq = 2, turnId = "t1", tool = "subjects", label = "Revisando"), store.sessionMark())
		assertEquals(AssistantTurnStatus.RUNNING, store.observeConversation().value.turns.getValue("t1").status)
	}

	@Test
	fun aTurnThatEndedIsNotMarkedUnconfirmed() = runTest {
		val store = AssistantConversationDataSource()
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(RunFinished(seq = 2, turnId = "t1"), store.sessionMark())

		store.markUnconfirmed(turnId = "t1", lastSeq = 1L, store.sessionMark())

		assertEquals(AssistantTurnStatus.FINISHED, store.observeConversation().value.turns.getValue("t1").status)
	}

	@Test
	fun applyingAnEventTwiceDoesNotApplyItTwice() = runTest {
		val store = AssistantConversationDataSource()

		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message), store.sessionMark())
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message), store.sessionMark())
		store.apply(Progress(seq = 1, turnId = "t1", tool = "subjects", label = "Tarde"), store.sessionMark())

		val state = store.observeConversation().value

		assertEquals(listOf(message), state.messages)
		assertEquals(2L, state.turns.getValue("t1").lastSeq)
		assertNull(state.turns.getValue("t1").progressLabel)
	}

	@Test
	fun progressIntentsAndTheEndOfTheTurnAreTracked() = runTest {
		val store = AssistantConversationDataSource()

		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(Progress(seq = 2, turnId = "t1", tool = "subjects", label = "Buscando materias"), store.sessionMark())
		store.apply(IntentProposed(seq = 3, turnId = "t1", messageId = "m1", intent = intent), store.sessionMark())

		val proposed = store.observeConversation().value

		assertEquals("Buscando materias", proposed.turns.getValue("t1").progressLabel)
		assertEquals(setOf("i1"), proposed.openIntents.keys)
		assertEquals("m1", proposed.openIntents.getValue("i1").messageId)

		store.apply(
			IntentResolved(seq = 4, turnId = "t1", intentId = "i1", outcome = OutcomeKind.CONFIRMED),
			store.sessionMark()
		)
		store.apply(RunFinished(seq = 5, turnId = "t1"), store.sessionMark())

		val finished = store.observeConversation().value

		assertTrue(finished.openIntents.isEmpty())
		assertEquals(AssistantTurnStatus.FINISHED, finished.turns.getValue("t1").status)
		assertEquals(5L, finished.turns.getValue("t1").lastSeq)
		assertNull(finished.turns.getValue("t1").progressLabel)
	}

	@Test
	fun aProposalTheNextTurnSupersedesIsClosedByTheServersEvent_andOneTheStudentAlreadyDecidedKeepsItsOutcome() = runTest {
		val store = AssistantConversationDataSource()
		val decided = intent.copy(id = "i2")
		store.apply(IntentProposed(seq = 1, turnId = "t1", messageId = "m1", intent = intent), store.sessionMark())
		store.apply(IntentProposed(seq = 2, turnId = "t1", messageId = "m1", intent = decided), store.sessionMark())
		store.apply(RunFinished(seq = 3, turnId = "t1"), store.sessionMark())
		store.markIntentResolved(intentId = "i2", outcome = OutcomeKind.CONFIRMED, store.sessionMark())

		// The next turn replaces both proposals in its commit, while the student had already confirmed the second one.
		store.apply(RunStarted(seq = 1, turnId = "t2"), store.sessionMark())
		store.apply(
			IntentResolved(2, "t2", "i1", OutcomeKind.CANCELLED, status = IntentStatus.SUPERSEDED),
			store.sessionMark()
		)
		store.apply(
			IntentResolved(3, "t2", "i2", OutcomeKind.CANCELLED, status = IntentStatus.SUPERSEDED),
			store.sessionMark()
		)
		store.apply(RunFinished(seq = 4, turnId = "t2"), store.sessionMark())

		val state = store.observeConversation().value

		assertTrue(state.openIntents.isEmpty())
		assertEquals(mapOf("i2" to OutcomeKind.CONFIRMED, "i1" to OutcomeKind.CANCELLED), state.resolvedIntents)
	}

	@Test
	fun clearSessionMemoryEmptiesTheConversation() = runTest {
		val store = AssistantConversationDataSource()
		store.attachConversation("c1", store.sessionMark())
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message), store.sessionMark())

		store.clearSessionMemory()

		// Empty, in the generation that follows the one that ended.
		assertEquals(AssistantConversationState(sessionGeneration = 1), store.observeConversation().value)
	}

	@Test
	fun anAssistantMessageWrittenOutsideATurnIsAnItemWithItsIntentOpen() = runTest {
		val store = AssistantConversationDataSource()
		val proposal = AssistantMessage(
			id = "photo-1",
			parts = listOf(Text("Encontré 1 evaluación."), IntentPart(intent)),
			createdAtMillis = 2L
		)

		store.addAssistantMessage(proposal, intent, store.sessionMark())

		val state = store.observeConversation().value
		assertEquals(listOf(proposal), state.messages)
		assertEquals(intent, state.openIntents.getValue("i1").intent)
	}

	@Test
	fun theSameAssistantMessageIsNotWrittenTwice() = runTest {
		val store = AssistantConversationDataSource()
		val proposal = AssistantMessage(id = "photo-1", parts = listOf(Text("Hola")), createdAtMillis = 2L)

		store.addAssistantMessage(proposal, intent = null, store.sessionMark())
		store.addAssistantMessage(proposal, intent = null, store.sessionMark())

		assertEquals(listOf(proposal), store.observeConversation().value.messages)
	}

	// The writes of a session that has ended (the ones a request started in it answers after the end) leave the
	// conversation of the session that follows as it is.
	@Test
	fun writesMadeForAnEndedSessionLeaveTheNextConversationAsItIs() = runTest {
		val store = AssistantConversationDataSource()
		val mark = store.sessionMark()
		val proposal = AssistantMessage(id = "photo-1", parts = listOf(Text("Hola")), createdAtMillis = 2L)
		val report = IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.CONFIRMED)
		val outbox = AssistantIntentOutboxDataSource(store)

		store.clearSessionMemory()
		store.attachConversation("c-old", mark)
		store.apply(RunStarted(seq = 1, turnId = "t-old"), mark)
		store.markUnconfirmed(turnId = "t-old", lastSeq = 1L, sessionMark = mark)
		store.addAssistantMessage(proposal, intent, mark)
		store.markIntentResolved("i1", OutcomeKind.CONFIRMED, mark)
		outbox.stageOutcome(report, mark)

		assertEquals(AssistantConversationState(sessionGeneration = 1), store.observeConversation().value)
		assertTrue(outbox.pendingOutcomes().isEmpty())
	}

	@Test
	fun theCountOfAResolvedAdditionIsKeptWithItsOutcome() = runTest {
		val store = AssistantConversationDataSource()

		val count = IntentResolvedCount(applied = 1, selected = 2)

		store.markIntentResolved("i1", OutcomeKind.CONFIRMED, store.sessionMark(), count)

		val state = store.observeConversation().value
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), state.resolvedIntents)
		assertEquals(mapOf("i1" to IntentResolvedCount(applied = 1, selected = 2)), state.resolvedCounts)
	}

	@Test
	fun aResolutionWithoutACountDropsTheCountKeptBefore() = runTest {
		val store = AssistantConversationDataSource()
		val mark = store.sessionMark()
		store.markIntentResolved("i1", OutcomeKind.QUEUED, mark, IntentResolvedCount(applied = 1, selected = 2))

		store.markIntentResolved("i1", OutcomeKind.CONFLICT, mark, count = null)

		val state = store.observeConversation().value
		assertEquals(mapOf("i1" to OutcomeKind.CONFLICT), state.resolvedIntents)
		assertTrue(state.resolvedCounts.isEmpty())
	}

	@Test
	fun aCountWrittenForAnEndedSessionIsNotKept() = runTest {
		val store = AssistantConversationDataSource()
		val mark = store.sessionMark()

		store.clearSessionMemory()
		store.markIntentResolved("i1", OutcomeKind.CONFIRMED, mark, IntentResolvedCount(applied = 1, selected = 2))

		assertEquals(AssistantConversationState(sessionGeneration = 1), store.observeConversation().value)
	}

	@Test
	fun clearingTheSessionForgetsItsCounts() = runTest {
		val store = AssistantConversationDataSource()
		val mark = store.sessionMark()
		store.markIntentResolved("i1", OutcomeKind.CONFIRMED, mark, IntentResolvedCount(applied = 1, selected = 2))

		store.clearSessionMemory()

		assertEquals(AssistantConversationState(sessionGeneration = 1), store.observeConversation().value)
		assertTrue(store.observeConversation().value.resolvedCounts.isEmpty())
	}

	// The same writes made for the session that is running are kept: the fence drops only what has ended.
	@Test
	fun writesMadeForTheRunningSessionAreKept() = runTest {
		val store = AssistantConversationDataSource()
		val outbox = AssistantIntentOutboxDataSource(store)
		val mark = store.sessionMark()

		store.attachConversation("c1", mark)
		outbox.stageOutcome(IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.CONFIRMED), mark)

		assertEquals("c1", store.observeConversation().value.conversationId)
		assertEquals(listOf("i1"), outbox.pendingOutcomes().map { report -> report.intentId })
	}
}
