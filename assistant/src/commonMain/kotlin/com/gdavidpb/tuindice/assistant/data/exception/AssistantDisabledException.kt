package com.gdavidpb.tuindice.assistant.data.exception

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes

/**
 * The server answered that the assistant is off for now.
 * [retryAfterSeconds] is the wait it asked for, when it gave one.
 */
class AssistantDisabledException(
	retryAfterSeconds: Int?
) : AssistantRequestRefusedException(
	code = AssistantErrorCodes.ASSISTANT_DISABLED,
	retryAfterSeconds = retryAfterSeconds,
	message = "The assistant is disabled by the server"
)
