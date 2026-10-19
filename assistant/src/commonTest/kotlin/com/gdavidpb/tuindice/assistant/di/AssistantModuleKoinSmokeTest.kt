package com.gdavidpb.tuindice.assistant.di

import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantMemoryDataSource
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class AssistantModuleKoinSmokeTest {

	@Test
	fun resolvesTheAssistantDataEntryPointsWithBothHttpClients() {
		withKoinSmokeTest(
			assistantModule,
			module {
				single<HttpClient> {
					assistantHttpClient { respond(content = "", status = HttpStatusCode.NoContent) }
				}
				single<HttpClient>(named(ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER)) {
					assistantHttpClient { respond(content = "", status = HttpStatusCode.NoContent) }
				}
				single<ConfigRepository> { FakeAssistantConfigRepository() }
				single<Settings> { FakeSettings() }
				single<Clock> { Clock.System }
			}
		) {
			assertResolves(
				AssistantEventsDataRepository::class,
				AssistantAvailabilityRepository::class,
				AssistantConsentRepository::class,
				AssistantInsightsRepository::class,
				AssistantMemoryDataSource::class,
				AssistantConversationDataSource::class
			)

			// Availability, consent, insights and conversation keep session state or answer for it.
			assertEquals(4, getAll<SessionMemory>().size)
		}
	}
}
