package com.gdavidpb.tuindice.base.domain.repository

/** Deletes every conversation the student has had with the assistant, on the server and on this device. */
interface AssistantConversationsRepository {
	suspend fun deleteAssistantConversations()
}
