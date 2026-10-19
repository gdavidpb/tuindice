package com.gdavidpb.tuindice.assistant.presentation.di

import com.gdavidpb.tuindice.assistant.di.assistantModule
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantPlansRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.testing.RecordingNotificationOfferRepository
import com.gdavidpb.tuindice.assistant.presentation.testing.RecordingNotificationPermissionRepository
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingImageEncoderRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
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

// The chat's graph resolves in the module: the view model with its machine, its collaborators and both texts.
class AssistantChatKoinSmokeTest {
	@Test
	fun resolvesTheChatViewModelWithItsMachineAndTexts() {
		withKoinSmokeTest(
			assistantModule,
			module {
				single<HttpClient> {
					assistantHttpClient { respond(content = "", status = HttpStatusCode.NoContent) }
				}
				single<HttpClient>(named(ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER)) {
					assistantHttpClient { respond(content = "", status = HttpStatusCode.NoContent) }
				}
				single<AssistantConfigRepository> { FakeAssistantConfigRepository() }
				single<Settings> { FakeSettings() }
				single<Clock> { Clock.System }
				single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
				single<ReportingRepository> { RecordingReportingRepository() }
				single<EventPublisher> { NoOpEventPublisher }
				single<TuIndiceDispatchers> { TestTuIndiceDispatchers(Dispatchers.Unconfined) }

				// The ports that maincore implements for the chat: the module only declares them.
				single<AssistantClientStateRepository> { FakeAssistantClientStateRepository() }
				single<AssistantEvaluationsRepository> { RecordingAssistantEvaluationsRepository() }
				single<AssistantQueuedMutationsRepository> { RecordingAssistantQueuedMutationsRepository() }
				single<AssistantPlansRepository> { RecordingAssistantPlansRepository() }
				single<AssistantProjectionRepository> { RecordingAssistantProjectionRepository() }
				single<ImageEncoderRepository> { RecordingImageEncoderRepository() }
				single<NotificationOfferRepository> { RecordingNotificationOfferRepository() }
				single<NotificationPermissionRepository> { RecordingNotificationPermissionRepository(offers = get()) }
			}
		) {
			assertResolves(
				AssistantChatViewModel::class,
				AssistantChatMachine::class,
				AssistantChatTexts::class,
				AssistantChatFeedbackTexts::class
			)
		}
	}
}
