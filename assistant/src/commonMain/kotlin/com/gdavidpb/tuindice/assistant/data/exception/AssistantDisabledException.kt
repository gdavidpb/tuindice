package com.gdavidpb.tuindice.assistant.data.exception

/**
 * The server answered that the assistant is off for now.
 * [retryAfterSeconds] is the wait it asked for, when it gave one.
 */
class AssistantDisabledException(
	val retryAfterSeconds: Int?
) : Exception("The assistant is disabled by the server")
