package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorBody
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString

private const val UNKNOWN_ERROR_CODE = "unknown"

/**
 * Turns an error answer of the assistant into the exception the data layer throws. It only reads the answer: a caller
 * that gets an [AssistantDisabledException] holds the assistant off for the session its request belonged to.
 */
internal suspend fun HttpResponse.toAssistantException(): Exception {
	val body = readAssistantErrorBody()
	val retryAfterSeconds = body?.retryAfterSeconds ?: headers[HttpHeaders.RetryAfter]?.toIntOrNull()

	if (status == HttpStatusCode.ServiceUnavailable && body?.code == AssistantErrorCodes.ASSISTANT_DISABLED) {
		return AssistantDisabledException(retryAfterSeconds)
	}

	return AssistantApiException(
		code = body?.code ?: UNKNOWN_ERROR_CODE,
		retryAfterSeconds = retryAfterSeconds,
		httpStatus = status.value
	)
}

private suspend fun HttpResponse.readAssistantErrorBody(): AssistantErrorBody? {
	return runCatching { ChatContract.json.decodeFromString<AssistantErrorBody>(bodyAsText()) }.getOrNull()
}
