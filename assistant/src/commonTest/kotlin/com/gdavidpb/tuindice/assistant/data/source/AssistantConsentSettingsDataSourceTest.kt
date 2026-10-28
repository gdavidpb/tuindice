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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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

		source.reconcileConsent(ConsentStatus(version = 2, accepted = true), source.consentSessionMark())

		assertTrue(source.assistantConsentGranted.value)
		assertTrue(AssistantConsentSettingsDataSource(settings, api).isAssistantConsentGranted())

		source.reconcileConsent(ConsentStatus(version = 2, accepted = false), source.consentSessionMark())

		assertFalse(source.assistantConsentGranted.value)
		assertFalse(AssistantConsentSettingsDataSource(settings, api).isAssistantConsentGranted())
	}

	// The answer of the server lands after the account has left: the stored consent and the observed one stay clear.
	@Test
	fun aDecisionAnsweredAfterTheSessionEndedLeavesNoConsent() = runTest {
		val settings = FakeSettings()
		val putStarted = CompletableDeferred<Unit>()
		val putAnswer = CompletableDeferred<Unit>()
		val client = assistantHttpClient { call ->
			if (call.method == HttpMethod.Get) {
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":false}""")
			} else {
				putStarted.complete(Unit)
				putAnswer.await()
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":true,"acceptedAtMillis":1700000000000}""")
			}
		}
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)

		val decision = launch { source.setAssistantConsentGranted(true) }
		putStarted.await()

		// The end of the session: the mirror follows the stored consent, then the wipe removes it, then the mirror again.
		source.clearSessionMemory()
		settings.clear()
		source.clearSessionMemory()
		putAnswer.complete(Unit)
		decision.join()

		assertFalse(settings.hasKey("assistantConsentGranted"))
		assertFalse(source.isAssistantConsentGranted())
		assertFalse(source.assistantConsentGranted.value)
	}

	@Test
	fun aRefreshAnsweredAfterTheSessionEndedLeavesNoConsent() = runTest {
		val settings = FakeSettings()
		val getStarted = CompletableDeferred<Unit>()
		val getAnswer = CompletableDeferred<Unit>()
		val client = assistantHttpClient {
			getStarted.complete(Unit)
			getAnswer.await()
			jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true}""")
		}
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)

		val refresh = launch { source.refreshAssistantConsent() }
		getStarted.await()

		source.clearSessionMemory()
		settings.clear()
		source.clearSessionMemory()
		getAnswer.complete(Unit)
		refresh.join()

		assertFalse(settings.hasKey("assistantConsentGranted"))
		assertFalse(source.assistantConsentGranted.value)
	}

	@Test
	fun aStatusReadBeforeTheSessionEndedIsNotKept() = runTest {
		val settings = FakeSettings()
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(assistantHttpClient { error("no request") }, RecordingServerDisabledRepository())
		)
		val mark = source.consentSessionMark()

		source.clearSessionMemory()
		source.reconcileConsent(ConsentStatus(version = 2, accepted = true), mark)

		assertFalse(settings.hasKey("assistantConsentGranted"))
		assertFalse(source.assistantConsentGranted.value)

		source.reconcileConsent(ConsentStatus(version = 2, accepted = true), source.consentSessionMark())

		assertTrue(source.assistantConsentGranted.value)
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

	@Test
	fun refreshingTheConsentKeepsWhatTheServerHoldsAfterAWipe() = runTest {
		val settings = FakeSettings()
		val api = KtorAssistantApiDataSource(
			assistantHttpClient { jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true}""") },
			RecordingServerDisabledRepository()
		)
		val source = AssistantConsentSettingsDataSource(settings, api)

		// A device that lost its stored consent, as after a reinstall or a sign-out, while the server holds it accepted.
		assertFalse(source.assistantConsentGranted.value)

		source.refreshAssistantConsent()

		assertTrue(source.assistantConsentGranted.value)
		assertTrue(AssistantConsentSettingsDataSource(settings, api).isAssistantConsentGranted())
	}

	@Test
	fun refreshingTheConsentWithoutAnswerLeavesItAsItWas() = runTest {
		val settings = FakeSettings()
		val ok = KtorAssistantApiDataSource(
			assistantHttpClient { jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true}""") },
			RecordingServerDisabledRepository()
		)
		val failing = KtorAssistantApiDataSource(
			assistantHttpClient { jsonAnswer(HttpStatusCode.InternalServerError, """{"code":"internal"}""") },
			RecordingServerDisabledRepository()
		)
		AssistantConsentSettingsDataSource(settings, ok).refreshAssistantConsent()
		val source = AssistantConsentSettingsDataSource(settings, failing)

		assertFailsWith<AssistantApiException> { source.refreshAssistantConsent() }

		assertTrue(source.assistantConsentGranted.value)
	}

	// A refresh that read the consent before a write landed does not store its answer: the write is the newer word.
	@Test
	fun aRefreshAnsweredAfterAWriteLandedDoesNotOverwriteIt() = runTest {
		val settings = FakeSettings()
		val refreshStarted = CompletableDeferred<Unit>()
		val refreshAnswer = CompletableDeferred<Unit>()
		var gets = 0
		val client = assistantHttpClient { call ->
			if (call.method == HttpMethod.Get) {
				gets++

				if (gets == 1) {
					// The refresh's own read: it is answered only after the write below has landed.
					refreshStarted.complete(Unit)
					refreshAnswer.await()
					jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true}""")
				} else {
					jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":false}""")
				}
			} else {
				jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":false,"acceptedAtMillis":1700000000000}""")
			}
		}
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)

		val refresh = launch { source.refreshAssistantConsent() }
		refreshStarted.await()

		source.setAssistantConsentGranted(false)
		refreshAnswer.complete(Unit)
		refresh.join()

		assertFalse(source.isAssistantConsentGranted())
		assertFalse(source.assistantConsentGranted.value)
		assertEquals(false, settings.getBooleanOrNull("assistantConsentGranted"))
	}

	// Two sets in a row: the second starts while the first waits for its answer. The server applies each decision when it
	// answers, so the second one must read the version the first one leaves. The device keeps the word of the last set.
	@Test
	fun aSetStartedWhileAnotherWaitsKeepsTheWordOfTheLastSet() = runTest {
		val settings = FakeSettings()
		var serverVersion = 1
		var serverAccepted = false
		var puts = 0
		val putStarted = CompletableDeferred<Unit>()
		val putAnswer = CompletableDeferred<Unit>()
		val client = assistantHttpClient(engineDispatcher = Dispatchers.Unconfined) { call ->
			if (call.method == HttpMethod.Get) {
				jsonAnswer(HttpStatusCode.OK, """{"version":$serverVersion,"accepted":$serverAccepted}""")
			} else {
				val accepted = (call.body as? TextContent)?.text?.contains("\"accepted\":true") == true
				puts++

				if (puts == 1) {
					putStarted.complete(Unit)
					putAnswer.await()
				}

				serverVersion += 1
				serverAccepted = accepted
				jsonAnswer(
					HttpStatusCode.OK,
					"""{"version":$serverVersion,"accepted":$serverAccepted,"acceptedAtMillis":1700000000000}"""
				)
			}
		}
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)

		val first = launch { source.setAssistantConsentGranted(true) }
		putStarted.await()

		val last = launch { source.setAssistantConsentGranted(false) }
		runCurrent()

		putAnswer.complete(Unit)
		joinAll(first, last)

		assertEquals(false, settings.getBooleanOrNull("assistantConsentGranted"))
		assertFalse(source.assistantConsentGranted.value)
	}

	// A status the server answered before a set landed is older than the set's consent: the reconcile keeps the set's.
	@Test
	fun aReconciledStatusReadBeforeASetLandedDoesNotOverwriteIt() = runTest {
		val settings = FakeSettings()
		val client = assistantHttpClient { call ->
			if (call.method == HttpMethod.Get) {
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":false}""")
			} else {
				jsonAnswer(HttpStatusCode.OK, """{"version":2,"accepted":true,"acceptedAtMillis":1700000000000}""")
			}
		}
		val source = AssistantConsentSettingsDataSource(
			settings,
			KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		)
		// The mark is taken before the status is requested; the student's set lands before the status is applied.
		val mark = source.consentSessionMark()
		val statusRead = ConsentStatus(version = 2, accepted = false)

		source.setAssistantConsentGranted(true)
		source.reconcileConsent(statusRead, mark)

		assertTrue(source.isAssistantConsentGranted())
		assertEquals(true, settings.getBooleanOrNull("assistantConsentGranted"))
	}
}
