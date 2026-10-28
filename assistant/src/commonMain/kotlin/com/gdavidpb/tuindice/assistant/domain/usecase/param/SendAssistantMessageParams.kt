package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlin.uuid.Uuid

data class SendAssistantMessageParams(
	val text: String?,
	val suggestionId: String?,
	val screenContext: AssistantScreenContext?,
	// The id the message keeps through its sends: a retry of a refused or unconfirmed send carries the id of that send,
	// so the server answers with the same turn instead of a new one. A retry of a run that failed is a new request, with
	// an id of its own.
	val clientTurnId: String = Uuid.random().toString(),
	// The question a retry replaces: the client turn id it was sent with, and the session the retry was decided in. The
	// question leaves the conversation in the same send, before the retry's question is stored; a session that has ended
	// since leaves the conversation as it is. Both are set together, or neither is.
	val replacesMessageId: String? = null,
	val replacesSessionMark: Int? = null
)
