package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantException
import com.gdavidpb.tuindice.chatcontract.ChatContract
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

// Runs one request. The client may fail an error answer by itself (expectSuccess): that answer is read from the
// exception, so both kinds of failure reach the same mapping.
internal suspend fun KtorAssistantApiDataSource.send(call: suspend HttpClient.() -> HttpResponse): HttpResponse {
	val response = try {
		httpClient.call()
	} catch (cause: ResponseException) {
		cause.response
	}

	if (response.status.isSuccess()) return response

	throw response.toAssistantException(serverDisabled)
}

internal inline fun <reified T> HttpRequestBuilder.setJsonBody(value: T) {
	setBody(TextContent(ChatContract.json.encodeToString(value), ContentType.Application.Json))
}

internal suspend inline fun <reified T> HttpResponse.decodeBody(): T {
	return ChatContract.json.decodeFromString(bodyAsText())
}

internal fun assistantPath(vararg segments: String): String {
	return (listOf("assistant", "v1") + segments).joinToString(separator = "/") { segment ->
		segment.encodeURLPathPart()
	}
}
