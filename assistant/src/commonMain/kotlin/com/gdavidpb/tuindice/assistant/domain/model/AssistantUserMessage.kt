package com.gdavidpb.tuindice.assistant.domain.model

/** A message the user sent. Its [id] is the client turn id it went with. */
data class AssistantUserMessage(
	val id: String,
	val text: String,
	val createdAtMillis: Long
)
