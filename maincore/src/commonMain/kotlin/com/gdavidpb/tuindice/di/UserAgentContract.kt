package com.gdavidpb.tuindice.di

fun buildStructuredUserAgent(
	appVersionName: String,
	appVersionCode: Long,
	osName: String,
	osVersion: String,
	osCode: Int,
	osId: String,
	manufacturer: String,
	model: String,
	appName: String = "TuIndice"
): String {
	return listOf(
		sanitizeUserAgentSegment(appName, fallback = "TuIndice"),
		sanitizeUserAgentSegment(appVersionName, fallback = "0.0.0"),
		appVersionCode.coerceAtLeast(0L).toString(),
		sanitizeUserAgentSegment(osName, fallback = "Unknown"),
		sanitizeUserAgentSegment(osVersion, fallback = "Unknown"),
		osCode.coerceAtLeast(0).toString(),
		sanitizeUserAgentSegment(osId, fallback = "Unknown"),
		sanitizeUserAgentSegment(manufacturer, fallback = "Unknown"),
		sanitizeUserAgentSegment(model, fallback = "Unknown")
	).joinToString(";")
}

private fun sanitizeUserAgentSegment(
	value: String,
	fallback: String
): String {
	val trimmedValue = value.trim()

	if (trimmedValue.isBlank() || trimmedValue.all { char -> char == ';' }) {
		return fallback
	}

	return trimmedValue.replace(";", "-")
}
