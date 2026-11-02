package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.security.data.source.network.AttestationHeaders
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.encodedPath
import io.ktor.http.parseQueryString
import io.ktor.client.plugins.logging.Logger as KtorLogger

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

// The logging of the client, as the app configures it. The requests to the attachments path are left out of the log:
// the plugin would write the body of an upload in full, and bodyFilter does not cover it (Ktor 3.6.0). Only the bytes
// of an attachment go that way, so no other request is affected.
internal fun HttpClientConfig<*>.installKtorLogging(logger: KtorLogger) {
	install(Logging) {
		this.logger = logger
		level = LogLevel.ALL

		filter { request -> !request.url.encodedPath.endsWith(ATTACHMENTS_PATH) }

		sanitizeHeader { header ->
			header == HttpHeaders.Authorization ||
					header == AttestationHeaders.ATTESTATION_TOKEN
		}
	}
}

private const val ATTACHMENTS_PATH = "assistant/v1/attachments"

internal fun redactSensitiveKtorLogMessage(message: String): String {
	return KtorLogSanitizer.sanitize(message)
}

private object KtorLogSanitizer {
	private val sensitiveTokenFieldNames = listOf(
		"access_token",
		"accessToken",
		"refresh_token",
		"refreshToken"
	)

	private val sensitiveFieldNames = sensitiveTokenFieldNames
		.map(String::lowercase)
		.toSet()

	private val jsonRedactionRules = sensitiveTokenFieldNames
		.map(::jsonTokenFieldRule)

	private val fallbackRedactionRules = buildList {
		addAll(jsonRedactionRules)
		addAll(sensitiveTokenFieldNames.map(::queryTokenFieldRule))
		add(
			SensitiveRedactionRule(
				marker = "Authorization: Bearer ",
				expression = "(?<=Authorization: Bearer )[^\r\n]+".toRegex(RegexOption.IGNORE_CASE)
			)
		)
	}

	fun sanitize(message: String): String {
		if (message.isBlank()) {
			return message
		}

		if (!message.containsSensitiveTokenMarker()) {
			return message
		}

		sanitizeJsonText(message)?.let { return it }

		return message
			.lines()
			.joinToString(separator = "\n") { line ->
				if (line.containsSensitiveTokenMarker()) line.sanitizeWholeLine() else line
			}
			.applySensitiveRules(fallbackRedactionRules)
	}

	private fun String.sanitizeWholeLine(): String {
		val trimmed = trim()
		val sanitized = sanitizeJsonText(trimmed) ?: sanitizeFormText(trimmed) ?: return this

		return replace(trimmed, sanitized)
	}

	private fun sanitizeJsonText(text: String): String? {
		val trimmed = text.trim()

		if (!trimmed.looksLikeJsonText()) {
			return null
		}

		return trimmed.applySensitiveRules(jsonRedactionRules)
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

	private fun String.containsSensitiveTokenMarker(): Boolean {
		return fallbackRedactionRules.any { rule ->
			contains(rule.marker, ignoreCase = true)
		}
	}
}

private fun String.looksLikeJsonText(): Boolean =
	startsWith("{") || startsWith("[")

private fun String.replaceSensitiveMatch(regex: Regex, valueGroup: Int): String {
	return regex.replace(this) { match ->
		match.groupValues.mapIndexed { index, group ->
			if (index == valueGroup) REDACTED_LOG_VALUE else group
		}.drop(1).joinToString(separator = "")
	}
}

private fun String.applySensitiveRules(
	rules: List<SensitiveRedactionRule>,
	replacement: String = REDACTED_LOG_VALUE
): String {
	return rules.fold(this) { current, rule ->
		if (rule.valueGroup == null) {
			current.replace(rule.expression, replacement)
		} else {
			current.replaceSensitiveMatch(rule.expression, valueGroup = rule.valueGroup)
		}
	}
}

private fun jsonTokenFieldRule(fieldName: String): SensitiveRedactionRule {
	val escapedFieldName = Regex.escape(fieldName)

	return SensitiveRedactionRule(
		marker = fieldName,
		expression = """(?i)(["']$escapedFieldName["']\s*:\s*["'])([^"']*)(["'])""".toRegex(),
		valueGroup = 2
	)
}

private fun queryTokenFieldRule(fieldName: String): SensitiveRedactionRule {
	val escapedFieldName = Regex.escape(fieldName)

	return SensitiveRedactionRule(
		marker = "$fieldName=",
		expression = "(?<=$escapedFieldName=)[^&\\s]+".toRegex(RegexOption.IGNORE_CASE)
	)
}

private data class SensitiveRedactionRule(
	val marker: String,
	val expression: Regex,
	val valueGroup: Int? = null
)
