package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlin.uuid.Uuid

data class SendAssistantMessageParams(
	val text: String?,
	val suggestionId: String?,
	val screenContext: AssistantScreenContext?,
	// The id the message keeps through its sends: a retry carries the id of the failed one, so the server answers with the
	// same turn instead of a new one.
	val clientTurnId: String = Uuid.random().toString()
)
