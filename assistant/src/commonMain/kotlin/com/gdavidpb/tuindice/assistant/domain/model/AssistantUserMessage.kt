package com.gdavidpb.tuindice.assistant.domain.model

/**
 * A message the user sent. Its [id] is the client turn id it went with, and [attachments] are the files it carried, as
 * the student sent them.
 */
data class AssistantUserMessage(
	val id: String,
	val text: String,
	val createdAtMillis: Long,
	val attachments: List<AssistantUserAttachment> = emptyList()
)
