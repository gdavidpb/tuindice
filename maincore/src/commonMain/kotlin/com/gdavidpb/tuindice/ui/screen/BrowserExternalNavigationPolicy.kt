package com.gdavidpb.tuindice.ui.screen

internal fun shouldOpenExternalResource(
	initialUrl: String,
	currentUrl: String?,
	requestedUrl: String?
): Boolean {
	val normalizedRequestedUrl = requestedUrl?.takeIf { it.isNotBlank() } ?: return false
	if (normalizedRequestedUrl.startsWith("#")) return false
	if (normalizedRequestedUrl == initialUrl) return false
	if (isSameDocumentNavigation(initialUrl, normalizedRequestedUrl)) return false

	val normalizedCurrentUrl = currentUrl?.takeIf { it.isNotBlank() } ?: return false
	return normalizedRequestedUrl != normalizedCurrentUrl
			&& !isSameDocumentNavigation(normalizedCurrentUrl, normalizedRequestedUrl)
}

internal fun shouldLoadBrowserUrl(
	currentUrl: String?,
	targetUrl: String
): Boolean {
	val normalizedCurrentUrl = currentUrl?.takeIf { it.isNotBlank() } ?: return true
	if (normalizedCurrentUrl == targetUrl) return false

	return !isSameDocumentNavigation(normalizedCurrentUrl, targetUrl)
}

private fun isSameDocumentNavigation(
	sourceUrl: String,
	requestedUrl: String
): Boolean {
	val sourceDocumentUrl = sourceUrl.substringBefore("#")
	val requestedDocumentUrl = requestedUrl.substringBefore("#")

	return sourceDocumentUrl == requestedDocumentUrl &&
			(sourceUrl.contains("#") || requestedUrl.contains("#"))
}
