package com.gdavidpb.tuindice.assistant.domain.repository

/** Holds the assistant off until the retry time the server gave when it answered that the assistant is disabled. */
interface AssistantServerDisabledRepository {
	fun markServerDisabled(retryAfterSeconds: Int?)
}
