package com.gdavidpb.tuindice.assistant.data.repository

/**
 * Receives the server's switch-off answer (503 assistant_disabled), so the availability holds the assistant off
 * until the retry time the server gave.
 */
interface AssistantServerDisabledDataRepository {
	fun markServerDisabled(retryAfterSeconds: Int?)
}
