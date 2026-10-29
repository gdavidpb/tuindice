package com.gdavidpb.tuindice.summary.presentation.mapper

// GCS signed URLs rotate their signature on every backend fetch, so the raw URL cannot act as the picture identity; the
// identity keeps the path and any non-signature parameters (e.g. a generation marker). The backend signs with V2, whose
// query is GoogleAccessId, Expires and Signature; a V4 signature is the X-Goog-* parameters. Both are dropped.
fun profilePictureIdentity(url: String): String {
	val queryStart = url.indexOf('?')
	if (queryStart < 0) return url

	val base = url.substring(0, queryStart)
	val stableQuery = url.substring(queryStart + 1)
		.split('&')
		.filterNot { parameter -> parameter.isSignatureParameter() }
		.joinToString(separator = "&")

	return if (stableQuery.isEmpty()) base else "$base?$stableQuery"
}

private fun String.isSignatureParameter(): Boolean {
	val name = substringBefore('=')

	return name.startsWith(prefix = V4_SIGNATURE_PREFIX, ignoreCase = true) ||
		V2_SIGNATURE_PARAMETERS.any { parameter -> name.equals(parameter, ignoreCase = true) }
}

private const val V4_SIGNATURE_PREFIX = "X-Goog-"
private val V2_SIGNATURE_PARAMETERS = listOf("GoogleAccessId", "Expires", "Signature")

// The key the image cache keeps a picture under: its identity and its version, so a re-upload behind a stable path
// still invalidates the cached bitmap while a signature-only rotation keeps hitting it. The summary and the avatar of
// the shell share it, so the avatar finds the bitmap the summary already loaded.
fun profilePictureCacheKey(url: String, version: Int): String = "${profilePictureIdentity(url)}#v$version"
