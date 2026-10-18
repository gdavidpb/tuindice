package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import io.ktor.http.HttpMethod
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
			if (call.method == HttpMethod.Get) {
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":false}""")
			} else {
				sentBody = (call.body as? TextContent)?.text
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":true,"acceptedAtMillis":1700000000000}""")
			}
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		val source = AssistantConsentSettingsDataSource(settings, api)

		source.setAssistantConsentGranted(true)

		// The version sent is the one the server holds now, not one the app keeps.
		assertEquals("""{"version":2,"accepted":true}""", sentBody)
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
	fun theStatusOfTheServerIsTheConsentTheDeviceKeeps() = runTest {
		val settings = FakeSettings()
		val api = KtorAssistantApiDataSource(assistantHttpClient { error("no request") }, RecordingServerDisabledRepository())
		val source = AssistantConsentSettingsDataSource(settings, api)

		source.reconcileConsent(ConsentStatus(version = 2, accepted = true))

		assertTrue(source.assistantConsentGranted.value)
		assertTrue(AssistantConsentSettingsDataSource(settings, api).isAssistantConsentGranted())

		source.reconcileConsent(ConsentStatus(version = 2, accepted = false))

		assertFalse(source.assistantConsentGranted.value)
		assertFalse(AssistantConsentSettingsDataSource(settings, api).isAssistantConsentGranted())
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
