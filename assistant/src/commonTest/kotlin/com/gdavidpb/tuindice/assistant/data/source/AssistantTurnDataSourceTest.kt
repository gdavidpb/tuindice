package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantTurnDataSourceTest {

	// The events of the contract reach the domain. The app's own event is written into the conversation, and never reaches
	// the domain.
	@Test
	fun contractEventsReachTheDomainAndTheOwnEventsAreWritten() = runTest {
		val conversation = AssistantConversationDataSource()
		val source = turnSource(
			conversation,
			listOf(
				AssistantStreamEvent.Contract(RunStarted(1, "t1")),
				AssistantStreamEvent.Own.UnknownIntentProposal(2, "t1", "m1"),
				AssistantStreamEvent.Contract(RunFinished(3, "t1", UsageSummary(9, 39, 399)))
			)
		)

		val delivered = source.turnEvents("t1", afterSeq = 0, sessionMark = conversation.sessionMark()).toList()

		assertEquals(listOf(1L, 3L), delivered.map { it.seq })
		assertEquals(setOf("m1"), conversation.observeConversation().value.unreadableMessages)
	}

	// An app's own event belongs to the session its follow started in: once that session has ended, it writes nothing.
	@Test
	fun anOwnEventOfAnEndedSessionWritesNothing() = runTest {
		val conversation = AssistantConversationDataSource()
		val source = turnSource(
			conversation,
			listOf(AssistantStreamEvent.Own.UnknownIntentProposal(1, "t1", "m1"))
		)
		val staleMark = conversation.sessionMark()
		conversation.clearSessionMemory()

		source.turnEvents("t1", afterSeq = 0, sessionMark = staleMark).toList()

		assertTrue(conversation.observeConversation().value.unreadableMessages.isEmpty())
	}

	private fun turnSource(
		conversation: AssistantConversationDataSource,
		events: List<AssistantStreamEvent>
	): AssistantTurnDataSource {
		return AssistantTurnDataSource(
			api = KtorAssistantApiDataSource(
				httpClient = assistantHttpClient { jsonAnswer(HttpStatusCode.OK, "{}") },
				serverDisabled = RecordingServerDisabledRepository()
			),
			eventsRepository = ScriptedEventsRepository(events),
			conversation = conversation
		)
	}

	private class ScriptedEventsRepository(
		private val scripted: List<AssistantStreamEvent>
	) : AssistantEventsDataRepository {
		override fun events(turnId: String, afterSeq: Long): Flow<AssistantStreamEvent> = scripted.asFlow()
	}
}
