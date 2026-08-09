package com.gdavidpb.tuindice.data.source.session

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64

// The gateway rejects tokens at the edge by their JWT `exp` claim; refreshing this
// far ahead keeps requests already in flight from racing the expiry under clock skew.
internal const val ACCESS_TOKEN_REFRESH_MARGIN_SECONDS = 300L

internal fun isAccessTokenExpiring(
	accessToken: String,
	nowEpochSeconds: Long = currentTimeMillis() / MILLIS_PER_SECOND
): Boolean {
	val expirationEpochSeconds = jwtExpirationEpochSeconds(accessToken) ?: return false

	return nowEpochSeconds >= expirationEpochSeconds - ACCESS_TOKEN_REFRESH_MARGIN_SECONDS
}

// A token without a readable `exp` claim reports no expiration: local mocks use opaque
// tokens, and the reactive 401 path stays the authority for anything unparsable.
internal fun jwtExpirationEpochSeconds(accessToken: String): Long? {
	val payloadSegment = accessToken.split('.').getOrNull(1) ?: return null

	return runCatching {
		val payloadJson = Base64.UrlSafe
			.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
			.decode(payloadSegment)
			.decodeToString()

		Json.parseToJsonElement(payloadJson)
			.jsonObject["exp"]
			?.jsonPrimitive
			?.longOrNull
	}.getOrNull()
}

private const val MILLIS_PER_SECOND = 1000L
