package com.gdavidpb.tuindice.assistant.data.exception

/** An error answer of the assistant API: its [code] (see AssistantErrorCodes) and the HTTP status it came with. */
class AssistantApiException(
	val code: String,
	val retryAfterSeconds: Int?,
	val httpStatus: Int
) : Exception("Assistant API error $httpStatus: $code")
