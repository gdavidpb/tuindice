package com.gdavidpb.tuindice.assistant.domain.repository

/** Holds the assistant off until the retry time the server gave when it answered that the assistant is disabled. */
interface AssistantServerDisabledRepository {
	/** The session the caller is in, read before its request starts; a switch-off answered after it ended is not held. */
	fun sessionMark(): Int

	fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int)
}
