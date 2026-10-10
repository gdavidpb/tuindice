package com.gdavidpb.tuindice.scenariokit.engine

import kotlin.io.encoding.Base64

internal object BasicAuth {
	private const val PREFIX = "Basic "

	/** `user:password` to a full `Authorization` header value. */
	fun header(credential: String): String = PREFIX + Base64.encode(credential.encodeToByteArray())

	/** The `user:password` an `Authorization: Basic` header carries, or null if it is not one. */
	fun decode(header: String?): String? =
		header
			?.takeIf { it.startsWith(PREFIX) }
			?.let { runCatching { Base64.decode(it.removePrefix(PREFIX)).decodeToString() }.getOrNull() }
}
