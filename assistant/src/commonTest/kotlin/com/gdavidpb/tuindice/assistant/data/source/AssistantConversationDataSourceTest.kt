package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantConversation
import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentBinding
import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
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
		store.apply(RunStarted(seq = 1, turnId = "t1"))
		store.apply(
			MessageCompleted(
				seq = 2,
				turnId = "t1",
				message = AssistantMessage(id = "m1", parts = listOf(Text("¿En qué te ayudo?")), createdAtMillis = 1_000L)
			)
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
		store.apply(RunStarted(seq = 1, turnId = "t1"))

		store.markUnconfirmed(turnId = "t1", lastSeq = 1L)
		assertEquals(AssistantTurnStatus.UNCONFIRMED, store.observeConversation().value.turns.getValue("t1").status)

		store.apply(Progress(seq = 2, turnId = "t1", tool = "subjects", label = "Revisando"))
		assertEquals(AssistantTurnStatus.RUNNING, store.observeConversation().value.turns.getValue("t1").status)
	}

	@Test
	fun aTurnThatEndedIsNotMarkedUnconfirmed() = runTest {
		val store = AssistantConversationDataSource()
		store.apply(RunStarted(seq = 1, turnId = "t1"))
		store.apply(RunFinished(seq = 2, turnId = "t1"))

		store.markUnconfirmed(turnId = "t1", lastSeq = 1L)

		assertEquals(AssistantTurnStatus.FINISHED, store.observeConversation().value.turns.getValue("t1").status)
	}

	@Test
	fun applyingAnEventTwiceDoesNotApplyItTwice() = runTest {
		val store = AssistantConversationDataSource()

		store.apply(RunStarted(seq = 1, turnId = "t1"))
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message))
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message))
		store.apply(Progress(seq = 1, turnId = "t1", tool = "subjects", label = "Tarde"))

		val state = store.observeConversation().value

		assertEquals(listOf(message), state.messages)
		assertEquals(2L, state.turns.getValue("t1").lastSeq)
		assertNull(state.turns.getValue("t1").progressLabel)
	}

	@Test
	fun progressIntentsAndTheEndOfTheTurnAreTracked() = runTest {
		val store = AssistantConversationDataSource()

		store.apply(RunStarted(seq = 1, turnId = "t1"))
		store.apply(Progress(seq = 2, turnId = "t1", tool = "subjects", label = "Buscando materias"))
		store.apply(IntentProposed(seq = 3, turnId = "t1", messageId = "m1", intent = intent))

		val proposed = store.observeConversation().value

		assertEquals("Buscando materias", proposed.turns.getValue("t1").progressLabel)
		assertEquals(setOf("i1"), proposed.openIntents.keys)
		assertEquals("m1", proposed.openIntents.getValue("i1").messageId)

		store.apply(IntentResolved(seq = 4, turnId = "t1", intentId = "i1", outcome = OutcomeKind.CONFIRMED))
		store.apply(RunFinished(seq = 5, turnId = "t1"))

		val finished = store.observeConversation().value

		assertTrue(finished.openIntents.isEmpty())
		assertEquals(AssistantTurnStatus.FINISHED, finished.turns.getValue("t1").status)
		assertEquals(5L, finished.turns.getValue("t1").lastSeq)
		assertNull(finished.turns.getValue("t1").progressLabel)
	}

	@Test
	fun clearSessionMemoryEmptiesTheConversation() = runTest {
		val store = AssistantConversationDataSource()
		store.attachConversation("c1")
		store.apply(RunStarted(seq = 1, turnId = "t1"))
		store.apply(MessageCompleted(seq = 2, turnId = "t1", message = message))

		store.clearSessionMemory()

		assertEquals(AssistantConversationState(), store.observeConversation().value)
	}

	@Test
	fun anAssistantMessageWrittenOutsideATurnIsAnItemWithItsIntentOpen() = runTest {
		val store = AssistantConversationDataSource()
		val proposal = AssistantMessage(
			id = "photo-1",
			parts = listOf(Text("Encontré 1 evaluación."), IntentPart(intent)),
			createdAtMillis = 2L
		)

		store.addAssistantMessage(proposal, intent)

		val state = store.observeConversation().value
		assertEquals(listOf(proposal), state.messages)
		assertEquals(intent, state.openIntents.getValue("i1").intent)
	}

	@Test
	fun theSameAssistantMessageIsNotWrittenTwice() = runTest {
		val store = AssistantConversationDataSource()
		val proposal = AssistantMessage(id = "photo-1", parts = listOf(Text("Hola")), createdAtMillis = 2L)

		store.addAssistantMessage(proposal, intent = null)
		store.addAssistantMessage(proposal, intent = null)

		assertEquals(listOf(proposal), store.observeConversation().value.messages)
	}
}
