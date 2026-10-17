package com.gdavidpb.tuindice.data.source.network

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.di.createSharedJson
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class OutdatedAppResponsePersistenceTest {
	@Test
	fun installOutdatedAppPersistence_persistsUpgradeRequiredState() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val outdatedAppEventRepository = OutdatedAppEventDataSource()
		val client = HttpClient(
			MockEngine {
				respond(
					content = """
						{
							"code": "outdated_app",
							"minimum_versions": {
								"android": 99999,
								"ios": 88888
							}
						}
					""".trimIndent(),
					status = HttpStatusCode.UpgradeRequired,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true

			install(ContentNegotiation) {
				json(createSharedJson())
			}

			installOutdatedAppPersistence(
				settingsRepository = settingsRepository,
				outdatedAppEventRepository = outdatedAppEventRepository,
				userAgentValue = "TuIndice;6.1.12;52;Android;17;37;id;Google;Pixel"
			)
		}
		// The events flow keeps nothing for a late collector and the request runs on the client's own threads: the
		// collector has to be subscribed before the request starts, not whenever the test scheduler gets to it.
		val event = async(start = CoroutineStart.UNDISPATCHED) {
			outdatedAppEventRepository.observeOutdatedApp().first()
		}

		try {
			assertFailsWith<ClientRequestException> {
				client.get("https://api.tuindice.app/auth/v2/bootstrap")
			}

			val expectedState = OutdatedAppState(minimumVersionCode = 99999)
			assertEquals(expectedState, settingsRepository.getOutdatedAppState())
			assertEquals(expectedState, event.await())
		} finally {
			client.close()
		}
	}

	@Test
	fun installOutdatedAppPersistence_persistsUpgradeRequiredState_withoutContentNegotiation() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val client = HttpClient(
			MockEngine {
				respond(
					content = """{"code":"outdated_app","minimum_versions":{"android":99999,"ios":88888}}""",
					status = HttpStatusCode.UpgradeRequired,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			// The assistant's stream client: no content negotiation.
			expectSuccess = true

			installOutdatedAppPersistence(
				settingsRepository = settingsRepository,
				outdatedAppEventRepository = OutdatedAppEventDataSource(),
				userAgentValue = "TuIndice;6.1.12;52;Android;17;37;id;Google;Pixel"
			)
		}

		try {
			assertFailsWith<ClientRequestException> {
				client.get("https://api.tuindice.app/assistant/v1/events/turns/turn-1")
			}

			assertEquals(OutdatedAppState(minimumVersionCode = 99999), settingsRepository.getOutdatedAppState())
		} finally {
			client.close()
		}
	}
}
