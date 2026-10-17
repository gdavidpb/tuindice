package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantConsentSettingsDataSourceTest {

	@Test
	fun acceptedDecisionIsObservedAndStored() = runTest {
		val settings = FakeSettings()
		var sentBody: String? = null
		val client = assistantHttpClient { call ->
			sentBody = (call.body as? TextContent)?.text
			jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true,"acceptedAtMillis":1700000000000}""")
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		val source = AssistantConsentSettingsDataSource(settings, api)

		source.setAssistantConsentGranted(true)

		assertEquals("""{"version":1,"accepted":true}""", sentBody)
		assertTrue(source.isAssistantConsentGranted())
		assertTrue(source.assistantConsentGranted.value)

		// A new instance over the same settings reads what was stored.
		val reopened = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)

		assertTrue(reopened.isAssistantConsentGranted())
	}

	@Test
	fun refusedDecisionKeepsTheLocalConsent() = runTest {
		val settings = FakeSettings()
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.InternalServerError, """{"code":"internal"}""")
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		val source = AssistantConsentSettingsDataSource(settings, api)

		assertFailsWith<AssistantApiException> { source.setAssistantConsentGranted(true) }

		assertFalse(source.isAssistantConsentGranted())
		assertFalse(source.assistantConsentGranted.value)
		assertFalse(settings.hasKey("assistantConsentGranted"))
	}

	@Test
	fun clearSessionMemoryRereadsTheStore() = runTest {
		val settings = FakeSettings()
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true}""")
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		val source = AssistantConsentSettingsDataSource(settings, api)
		source.setAssistantConsentGranted(true)

		// The stored consent goes with the data wipe, while the mirror still holds the old value.
		settings.clear()
		source.clearSessionMemory()

		assertFalse(source.isAssistantConsentGranted())
	}
}
