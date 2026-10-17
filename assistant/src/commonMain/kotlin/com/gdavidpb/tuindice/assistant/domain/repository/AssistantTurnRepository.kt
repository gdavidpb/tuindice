package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.coroutines.flow.Flow

/**
 * The assistant's turns: starting, following and cancelling them, and deleting the conversations on the server.
 */
interface AssistantTurnRepository {
	suspend fun sendTurn(request: TurnRequest): TurnAccepted

	suspend fun cancelTurn(turnId: String)

	/** The events of [turnId] from [afterSeq] on. The flow completes after the final event of the turn. */
	fun turnEvents(turnId: String, afterSeq: Long): Flow<AssistantEvent>

	suspend fun deleteConversations()
}
