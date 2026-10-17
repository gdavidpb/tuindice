package com.gdavidpb.tuindice.assistant.presentation.di

import com.gdavidpb.tuindice.assistant.di.assistantModule
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantPlansRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryTexts
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantMemoryViewModel
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.time.Clock

// The memory screen and the component sheet resolve in the module, each with its machine and its collaborators. The
// ports that maincore implements for the assistant are declared here as fakes, as the chat's smoke test does.
class AssistantMemoryKoinSmokeTest {
	@Test
	fun resolvesTheMemoryAndTheComponentSheetWithTheirMachines() {
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
				single<ReportingRepository> { RecordingReportingRepository() }
				single<EventPublisher> { NoOpEventPublisher }
				single<TuIndiceDispatchers> { TestTuIndiceDispatchers(Dispatchers.Unconfined) }

				// The ports that maincore implements for the assistant: the module only declares them.
				single<AssistantClientStateRepository> { FakeAssistantClientStateRepository() }
				single<AssistantEvaluationsRepository> { RecordingAssistantEvaluationsRepository() }
				single<AssistantPlansRepository> { RecordingAssistantPlansRepository() }
				single<AssistantProjectionRepository> { RecordingAssistantProjectionRepository() }
			}
		) {
			assertResolves(
				AssistantMemoryViewModel::class,
				AssistantMemoryMachine::class,
				AssistantMemoryTexts::class,
				AssistantComponentSheetViewModel::class,
				AssistantComponentSheetMachine::class
			)
		}
	}
}
