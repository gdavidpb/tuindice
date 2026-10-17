package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

data class SendAssistantMessageParams(
	val text: String?,
	val suggestionId: String?,
	val screenContext: AssistantScreenContext?
)
