package com.gdavidpb.tuindice.assistant.data.repository

/**
 * Receives the server's switch-off answer (503 assistant_disabled), so the availability holds the assistant off
 * until the retry time the server gave.
 */
interface AssistantServerDisabledDataRepository {
	/** The session a request is made in: read before the request starts, and given back to [markServerDisabled]. */
	fun sessionMark(): Int

	fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int)
}
