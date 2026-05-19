package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.logging.appLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseQueryString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

private const val HTTP_CLIENT_LOG_TAG = "HttpClient"
private const val REDACTED_LOG_VALUE = "***"

fun createAppKtorLogger(tag: String = HTTP_CLIENT_LOG_TAG): KtorLogger {
	val logger = appLogger(tag)

	return object : KtorLogger {
		override fun log(message: String) {
			logger.v { redactSensitiveKtorLogMessage(message) }
		}
	}
}

internal fun redactSensitiveKtorLogMessage(message: String): String {
	return KtorLogSanitizer.sanitize(message)
}

private object KtorLogSanitizer {
	private val json = Json {
		ignoreUnknownKeys = true
		isLenient = true
	}

	private val sensitiveFieldNames = setOf(
		"access_token",
		"accessToken",
		"refresh_token",
		"refreshToken"
	).map(String::lowercase).toSet()

	private val quotedTokenFieldRegex = Regex(
		pattern = """(?i)(["'](?:access_token|refresh_token|accessToken|refreshToken)["']\s*:\s*["'])([^"']*)(["'])"""
	)

	private val fallbackExpressions = listOf(
		"(?<=access_token=)[^&\\s]+".toRegex(RegexOption.IGNORE_CASE),
		"(?<=accessToken=)[^&\\s]+".toRegex(RegexOption.IGNORE_CASE),
		"(?<=refresh_token=)[^&\\s]+".toRegex(RegexOption.IGNORE_CASE),
		"(?<=refreshToken=)[^&\\s]+".toRegex(RegexOption.IGNORE_CASE),
		"(?<=Authorization: Bearer )[^\r\n]+".toRegex(RegexOption.IGNORE_CASE)
	)

	fun sanitize(message: String): String {
		if (message.isBlank()) {
			return message
		}

		sanitizeJsonText(message)?.let { return it }

		return message
			.lines()
			.joinToString(separator = "\n") { line ->
				line.sanitizeWholeLine()
			}
			.replaceSensitiveMatch(quotedTokenFieldRegex, valueGroup = 2)
			.replaceAll(fallbackExpressions)
	}

	private fun String.sanitizeWholeLine(): String {
		val trimmed = trim()
		val sanitized = sanitizeJsonText(trimmed) ?: sanitizeFormText(trimmed) ?: return this

		return replace(trimmed, sanitized)
	}

	private fun sanitizeJsonText(text: String): String? {
		val trimmed = text.trim()

		if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
			return null
		}

		val element = runCatching { json.parseToJsonElement(trimmed) }.getOrNull() ?: return null

		return json.encodeToString(
			JsonElement.serializer(),
			sanitizeJsonElement(element)
		)
	}

	private fun sanitizeJsonElement(element: JsonElement): JsonElement {
		return when (element) {
			is JsonArray ->
				JsonArray(
					element.map { item ->
						sanitizeJsonElement(item)
					}
				)

			is JsonObject ->
				JsonObject(
					element.jsonObject.mapValues { (name, value) ->
						if (name.lowercase() in sensitiveFieldNames)
							JsonPrimitive(REDACTED_LOG_VALUE)
						else
							sanitizeJsonElement(value)
					}
				)

			else -> element
		}
	}

	private fun sanitizeFormText(text: String): String? {
		if (!text.looksLikeFormBody()) {
			return null
		}

		val parameters = parseQueryString(text)

		if (parameters.isEmpty()) {
			return null
		}

		return parameters.entries()
			.joinToString("&") { (name, values) ->
				val isSensitive = name.lowercase() in sensitiveFieldNames
				val sanitizedValues = values.map { value ->
					if (isSensitive) REDACTED_LOG_VALUE else value
				}

				sanitizedValues.joinToString("&") { value ->
					val encodedValue = if (isSensitive) value else value.encodeURLQueryComponent()

					"${name.encodeURLQueryComponent()}=$encodedValue"
				}
			}
	}

	private fun String.looksLikeFormBody(): Boolean {
		return contains("=") &&
			!contains(" ") &&
			!contains("\n") &&
			!startsWith("http://") &&
			!startsWith("https://")
	}
}

private fun String.replaceSensitiveMatch(regex: Regex, valueGroup: Int): String {
	return regex.replace(this) { match ->
		match.groupValues.mapIndexed { index, group ->
			if (index == valueGroup) REDACTED_LOG_VALUE else group
		}.drop(1).joinToString(separator = "")
	}
}

private fun String.replaceAll(expressions: List<Regex>, replacement: String = REDACTED_LOG_VALUE): String =
	expressions.fold(this) { acc, regex -> acc.replace(regex, replacement) }
