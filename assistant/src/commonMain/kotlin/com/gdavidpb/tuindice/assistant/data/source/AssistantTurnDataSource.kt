package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

// The turns span two origins: the REST calls of the API, and the event stream of the turn.
class AssistantTurnDataSource(
	private val api: KtorAssistantApiDataSource,
	private val eventsRepository: AssistantEventsDataRepository,
	private val conversation: AssistantConversationDataSource
) : AssistantTurnRepository {
	override suspend fun sendTurn(request: TurnRequest): TurnAccepted = api.postTurn(request)

	override suspend fun cancelTurn(turnId: String) = api.postTurnCancel(turnId)

	// The events of the contract go on to the domain. The app's own events never reach it: they are written into the
	// conversation here, for the session the follow was started in, so the domain keeps to the contract's events.
	override fun turnEvents(turnId: String, afterSeq: Long, sessionMark: Int): Flow<AssistantEvent> {
		return eventsRepository.events(turnId, afterSeq).transform { event ->
			when (event) {
				is AssistantStreamEvent.Contract -> emit(event.event)

				is AssistantStreamEvent.Own -> conversation.applyOwn(event, sessionMark)
			}
		}
	}

	override suspend fun deleteConversations() = api.deleteThreads()
}
