package com.gdavidpb.tuindice.summary.presentation.mapper

// GCS signed URLs rotate their X-Goog-* query parameters on every backend fetch,
// so the raw URL cannot act as the picture identity; the identity keeps the path
// and any non-signature parameters (e.g. a future generation marker).
fun profilePictureIdentity(url: String): String {
	val queryStart = url.indexOf('?')
	if (queryStart < 0) return url

	val base = url.substring(0, queryStart)
	val stableQuery = url.substring(queryStart + 1)
		.split('&')
		.filterNot { parameter -> parameter.startsWith(prefix = "X-Goog-", ignoreCase = true) }
		.joinToString(separator = "&")

	return if (stableQuery.isEmpty()) base else "$base?$stableQuery"
}
