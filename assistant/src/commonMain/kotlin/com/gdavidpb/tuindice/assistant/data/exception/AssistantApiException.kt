package com.gdavidpb.tuindice.assistant.data.exception

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException

/**
 * An error answer of the assistant API: its [code] (see AssistantErrorCodes) and the HTTP status it came with.
 * It is the domain's refusal too, so the use cases read it without depending on the data layer.
 */
class AssistantApiException(
	code: String,
	retryAfterSeconds: Int?,
	val httpStatus: Int
) : AssistantRequestRefusedException(
	code = code,
	retryAfterSeconds = retryAfterSeconds,
	message = "Assistant API error $httpStatus: $code"
)
