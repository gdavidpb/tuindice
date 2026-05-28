package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.logging.appLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseQueryString

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
