package com.gdavidpb.tuindice.assistant.domain.exception

/**
 * The assistant refused a request with error [code] (see AssistantErrorCodes), and the wait it asked for, if any.
 * The data layer throws it, or a subclass, for every error answer, so the use cases map it without the data layer.
 */
open class AssistantRequestRefusedException(
	val code: String,
	val retryAfterSeconds: Int?,
	message: String
) : Exception(message)
