package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.context.ClientState

/** Implemented by maincore: the state of this client that the service needs to plan a turn. */
interface AssistantClientStateRepository {
	suspend fun clientState(): ClientState
}
