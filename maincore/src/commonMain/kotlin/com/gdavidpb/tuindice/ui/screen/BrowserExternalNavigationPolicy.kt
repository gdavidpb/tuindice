package com.gdavidpb.tuindice.ui.screen

internal fun shouldOpenExternalResource(
	initialUrl: String,
	currentUrl: String?,
	requestedUrl: String?
): Boolean {
	val normalizedRequestedUrl = requestedUrl?.takeIf { it.isNotBlank() } ?: return false
	if (normalizedRequestedUrl == initialUrl) return false

	val normalizedCurrentUrl = currentUrl?.takeIf { it.isNotBlank() } ?: return false
	return normalizedRequestedUrl != normalizedCurrentUrl
}
