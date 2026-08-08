package com.gdavidpb.tuindice.data.source.session

import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionTokenExpirationTest {
	@Test
	fun jwtExpirationEpochSeconds_readsExpClaimFromPayload() {
		assertEquals(1_754_600_000L, jwtExpirationEpochSeconds(jwtWithPayload("""{"exp":1754600000}""")))
	}

	@Test
	fun jwtExpirationEpochSeconds_readsExpClaimFromPaddedPayload() {
		val token = jwtWithPayload(
			payloadJson = """{"sub":"user-1","exp":1754600000}""",
			padding = Base64.PaddingOption.PRESENT
		)

		assertEquals(1_754_600_000L, jwtExpirationEpochSeconds(token))
	}

	@Test
	fun jwtExpirationEpochSeconds_returnsNullWithoutExpClaim() {
		assertNull(jwtExpirationEpochSeconds(jwtWithPayload("""{"sub":"user-1"}""")))
	}

	@Test
	fun jwtExpirationEpochSeconds_returnsNullForOpaqueTokens() {
		assertNull(jwtExpirationEpochSeconds("bootstrap.mock.access.token"))
		assertNull(jwtExpirationEpochSeconds("opaque-token"))
		assertNull(jwtExpirationEpochSeconds(""))
	}

	@Test
	fun isAccessTokenExpiring_flagsTokensInsideTheRefreshMargin() {
		val token = jwtWithPayload("""{"exp":1000}""")

		assertFalse(isAccessTokenExpiring(token, nowEpochSeconds = 699))
		assertTrue(isAccessTokenExpiring(token, nowEpochSeconds = 700))
		assertTrue(isAccessTokenExpiring(token, nowEpochSeconds = 1000))
		assertTrue(isAccessTokenExpiring(token, nowEpochSeconds = 5000))
	}

	@Test
	fun isAccessTokenExpiring_treatsUnparsableTokensAsNotExpiring() {
		assertFalse(isAccessTokenExpiring("bootstrap.mock.access.token", nowEpochSeconds = Long.MAX_VALUE))
	}
}

internal fun jwtWithPayload(
	payloadJson: String,
	padding: Base64.PaddingOption = Base64.PaddingOption.ABSENT
): String {
	val encoder = Base64.UrlSafe.withPadding(padding)
	val header = encoder.encode("""{"alg":"RS256","typ":"JWT"}""".encodeToByteArray())
	val payload = encoder.encode(payloadJson.encodeToByteArray())

	return "$header.$payload.signature"
}
