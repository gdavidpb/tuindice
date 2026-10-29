package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import io.ktor.client.request.delete
import io.ktor.client.request.post

internal suspend fun KtorAssistantApiDataSource.postTurn(request: TurnRequest): TurnAccepted {
	return send { post(assistantPath("turns")) { setJsonBody(request) } }.decodeBody()
}

internal suspend fun KtorAssistantApiDataSource.postTurnCancel(turnId: String) {
	send { post(assistantPath("turns", turnId, "cancel")) }
}

internal suspend fun KtorAssistantApiDataSource.deleteThreads() {
	send { delete(assistantPath("threads")) }
}
