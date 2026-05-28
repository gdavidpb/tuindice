package com.gdavidpb.tuindice.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KtorLoggingTest {
	@Test
	fun redactSensitiveKtorLogMessage_redactsJsonTokenFields_withoutDroppingTheLogMessage() {
		val message = """
			RESPONSE https://api.tuindice.app/auth/v2/token/refresh
			BODY START
			{"session_id":"session-123","access_token":"access-token","refresh_token":"refresh-token","scope":"record"}
			BODY END
		""".trimIndent()

		val redacted = redactSensitiveKtorLogMessage(message)

		assertTrue(redacted.contains("RESPONSE https://api.tuindice.app/auth/v2/token/refresh"))
		assertTrue(redacted.contains(""""session_id":"session-123""""))
		assertTrue(redacted.contains(""""access_token":"***""""))
		assertTrue(redacted.contains(""""refresh_token":"***""""))
		assertFalse(redacted.contains("access-token"))
		assertFalse(redacted.contains("refresh-token"))
	}

	@Test
	fun redactSensitiveKtorLogMessage_redactsNestedJsonTokenFields() {
		val message = """
			{
				"session_id":"session-123",
				"credentials":{"accessToken":"access-token"},
				"tokens":[{"refreshToken":"refresh-token"}]
			}
		""".trimIndent()

		val redacted = redactSensitiveKtorLogMessage(message)

		assertTrue(redacted.contains(""""session_id":"session-123""""))
		assertTrue(redacted.contains(""""accessToken":"***""""))
		assertTrue(redacted.contains(""""refreshToken":"***""""))
		assertFalse(redacted.contains("access-token"))
		assertFalse(redacted.contains("refresh-token"))
	}

	@Test
	fun redactSensitiveKtorLogMessage_redactsFormTokenFields() {
		val message = "access_token=form-access&refresh_token=form-refresh&scope=record"

		val redacted = redactSensitiveKtorLogMessage(message)

		assertTrue(redacted.contains("access_token=***"), redacted)
		assertTrue(redacted.contains("refresh_token=***"), redacted)
		assertTrue(redacted.contains("scope=record"), redacted)
		assertFalse(redacted.contains("form-access"))
		assertFalse(redacted.contains("form-refresh"))
	}

	@Test
	fun redactSensitiveKtorLogMessage_redactsCamelCaseAndQueryTokenValuesInMixedLogLines() {
		val message = """
			REQUEST https://api.tuindice.app/auth?access_token=query-access&refresh_token=query-refresh
			accessToken=body-access refreshToken=body-refresh
		""".trimIndent()

		val redacted = redactSensitiveKtorLogMessage(message)

		assertFalse(redacted.contains("query-access"))
		assertFalse(redacted.contains("query-refresh"))
		assertFalse(redacted.contains("body-access"))
		assertFalse(redacted.contains("body-refresh"))
		assertTrue(redacted.contains("access_token=***"))
		assertTrue(redacted.contains("refresh_token=***"))
		assertTrue(redacted.contains("accessToken=***"))
		assertTrue(redacted.contains("refreshToken=***"))
	}

	@Test
	fun redactSensitiveKtorLogMessage_redactsBearerTokenFallback() {
		val redacted = redactSensitiveKtorLogMessage("Authorization: Bearer access-token")

		assertEquals("Authorization: Bearer ***", redacted)
	}

	@Test
	fun redactSensitiveKtorLogMessage_leavesLargeJsonBodiesWithoutSensitiveKeysUntouched() {
		val body = buildString {
			append("""{"pensums":[""")
			repeat(17_000) { append("a") }
			append("]}")
		}

		val redacted = redactSensitiveKtorLogMessage(body)

		assertEquals(body, redacted)
	}

	@Test
	fun redactSensitiveKtorLogMessage_redactsLargeJsonBodiesOnlyWhenSensitiveKeysArePresent() {
		val body = buildString {
			append("{\"items\":[")
			repeat(17_000) { append("\"value\",") }
			append("""],"access_token":"access-token"}""")
		}

		val redacted = redactSensitiveKtorLogMessage(body)

		assertTrue(redacted.contains(""""access_token":"***""""))
		assertFalse(redacted.contains("access-token"))
	}

	@Test
	fun redactSensitiveKtorLogMessage_doesNotInspectLargeJsonBodyLineWhenOnlyMetadataNeedsRedaction() {
		val message = buildString {
			appendLine("RESPONSE https://api.tuindice.app/pensums/v4?access_token=query-access")
			appendLine("BODY START")
			append("{\"pensums\":[")
			repeat(17_000) { append("x") }
			append("]}")
			appendLine()
			append("BODY END")
		}

		val redacted = redactSensitiveKtorLogMessage(message)

		assertTrue(redacted.contains("access_token=***"))
		assertTrue(redacted.contains("\"pensums\""))
		assertFalse(redacted.contains("query-access"))
	}
}
