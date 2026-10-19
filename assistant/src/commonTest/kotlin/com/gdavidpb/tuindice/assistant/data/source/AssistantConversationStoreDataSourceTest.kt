package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssistantConversationStoreDataSourceTest {
	private val store = AssistantConversationDataSource()
	private val domain = AssistantConversationStoreDataSource(store)

	@Test
	fun itemsFollowTheOrderTheyHappenedIn_evenWhenTheirStampsTie() = runTest {
		domain.addUserMessage(AssistantUserMessage(id = "u1", text = "Primero", createdAtMillis = 1L))
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(
			MessageCompleted(seq = 2, turnId = "t1", message = assistantMessage(id = "a1", createdAtMillis = 2L)),
			store.sessionMark()
		)
		domain.addUserMessage(AssistantUserMessage(id = "u2", text = "Segundo", createdAtMillis = 2L))

		val ids = domain.currentConversation().items.map { item -> item.messageId() }

		// The answer came before the second message was sent, whatever the stamps say.
		assertEquals(listOf("u1", "a1", "u2"), ids)
	}

	@Test
	fun eachAgentMessageCarriesTheTurnThatWroteIt() = runTest {
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(
			MessageCompleted(seq = 2, turnId = "t1", message = assistantMessage(id = "a1", createdAtMillis = 1L)),
			store.sessionMark()
		)

		val agent = assertIs<AssistantConversationItem.Agent>(domain.currentConversation().items.single())

		assertEquals("t1", agent.turnId)
	}

	@Test
	fun aUserMessageIsWrittenOnceForItsId() = runTest {
		domain.addUserMessage(AssistantUserMessage(id = "u1", text = "Hola", createdAtMillis = 1L))
		domain.addUserMessage(AssistantUserMessage(id = "u1", text = "Hola otra vez", createdAtMillis = 5L))

		val items = domain.currentConversation().items

		assertEquals(1, items.size)
		assertEquals("Hola", assertIs<AssistantConversationItem.User>(items.single()).message.text)
	}

	@Test
	fun markingAnIntentResolvedClosesItAndKeepsItsOutcome() = runTest {
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(
			IntentProposed(seq = 2, turnId = "t1", messageId = "a1", intent = assistantIntent(id = "i1")),
			store.sessionMark()
		)

		assertEquals(setOf("i1"), domain.currentConversation().openIntents.keys)

		domain.markIntentResolved(intentId = "i1", outcome = OutcomeKind.CONFIRMED, domain.sessionMark())

		val conversation = domain.currentConversation()

		assertTrue(conversation.openIntents.isEmpty())
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolvedIntents)
	}

	@Test
	fun theLastTurnIsTheActiveOneAndItKeepsWhyItFailed() = runTest {
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(RunFinished(seq = 2, turnId = "t1"), store.sessionMark())
		store.apply(RunStarted(seq = 3, turnId = "t2"), store.sessionMark())
		store.apply(
			RunError(
				seq = 4,
				turnId = "t2",
				code = RunErrorCode.QUOTA_EXHAUSTED,
				retryable = false,
				retryAfterSeconds = 60
			),
			store.sessionMark()
		)

		assertEquals(
			AssistantActiveTurn(
				turnId = "t2",
				lastSeq = 4,
				progressLabel = null,
				status = AssistantTurnPhase.FAILED,
				failure = AssistantTurnFailure(
					code = RunErrorCode.QUOTA_EXHAUSTED,
					retryable = false,
					retryAfterSeconds = 60
				)
			),
			domain.currentConversation().activeTurn
		)
	}

	@Test
	fun aNewRunClearsThePreviousFailure() = runTest {
		store.apply(RunStarted(seq = 1, turnId = "t1"), store.sessionMark())
		store.apply(RunError(seq = 2, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true), store.sessionMark())
		store.apply(RunStarted(seq = 3, turnId = "t1"), store.sessionMark())

		val active = domain.currentConversation().activeTurn

		assertEquals(AssistantTurnPhase.RUNNING, active?.status)
		assertNull(active?.failure)
	}

	@Test
	fun clearingTheSessionEmptiesTheDomainConversationToo() = runTest {
		domain.attachConversation("c1", domain.sessionMark())
		domain.addUserMessage(AssistantUserMessage(id = "u1", text = "Hola", createdAtMillis = 1L))
		store.apply(RunStarted(seq = 2, turnId = "t1"), store.sessionMark())

		store.clearSessionMemory()

		assertEquals(
			AssistantConversation(
				conversationId = null,
				items = emptyList(),
				openIntents = emptyMap(),
				resolvedIntents = emptyMap(),
				activeTurn = null
			),
			domain.currentConversation()
		)
	}

	@Test
	fun observingEmitsTheConversationAsItStands() = runTest {
		domain.addUserMessage(AssistantUserMessage(id = "u1", text = "Hola", createdAtMillis = 1L))

		val observed = domain.observeConversation().first()

		assertEquals(domain.currentConversation(), observed)
	}

	private fun AssistantConversationItem.messageId(): String {
		return when (this) {
			is AssistantConversationItem.User -> message.id
			is AssistantConversationItem.Agent -> message.id
		}
	}

	private fun assistantMessage(id: String, createdAtMillis: Long) = AssistantMessage(
		id = id,
		parts = listOf(Text("Respuesta")),
		createdAtMillis = createdAtMillis
	)
}
