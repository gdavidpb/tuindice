package com.gdavidpb.tuindice.assistant.di

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantPlansRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.AcceptAssistantConsentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AnswerStillAppliesUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AppealAssistantRestrictionUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AssistantIntentConfirmer
import com.gdavidpb.tuindice.assistant.domain.usecase.AssistantTurnStream
import com.gdavidpb.tuindice.assistant.domain.usecase.CancelAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ClearAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmAddEvaluationsIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmCreatePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeleteEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeletePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditPlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmProjectionIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmRememberIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DelegateEnrollmentProofIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DeleteAssistantMemoryItemUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DismissAssistantIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.FollowAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantStatusUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.RateAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ReportAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ResumeAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageHandling
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.SendAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.FakeSettings
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.time.Clock

class AssistantDomainKoinSmokeTest {
	@Test
	fun resolvesEveryUseCaseAndItsRepositoriesWithTheMaincorePortsFaked() = withKoinSmokeTest(
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
			single<AssistantClientStateRepository> { FakeAssistantClientStateRepository() }
			single<AssistantEvaluationsRepository> { RecordingAssistantEvaluationsRepository() }
			single<AssistantQueuedMutationsRepository> { RecordingAssistantQueuedMutationsRepository() }
			single<AssistantPlansRepository> { RecordingAssistantPlansRepository() }
			single<AssistantProjectionRepository> { RecordingAssistantProjectionRepository() }
		}
	) {
		assertResolves(
			AssistantTurnRepository::class,
			AssistantConversationRepository::class,
			AssistantIntentRepository::class,
			AssistantFeedbackRepository::class,
			AssistantMemoryRepository::class,
			AssistantStandingRepository::class,
			AssistantServerDisabledRepository::class,
			AssistantIntentConfirmer::class,
			AssistantTurnStream::class,
			ObserveAssistantConversationUseCase::class,
			SendAssistantMessageUseCase::class,
			SendAssistantMessageHandling::class,
			SendAssistantMessageExceptionHandler::class,
			FollowAssistantTurnUseCase::class,
			ResumeAssistantTurnUseCase::class,
			CancelAssistantTurnUseCase::class,
			ConfirmAddEvaluationsIntentUseCase::class,
			ConfirmEditEvaluationIntentUseCase::class,
			ConfirmDeleteEvaluationIntentUseCase::class,
			ConfirmCreatePlanIntentUseCase::class,
			ConfirmEditPlanIntentUseCase::class,
			ConfirmDeletePlanIntentUseCase::class,
			ConfirmProjectionIntentUseCase::class,
			ConfirmRememberIntentUseCase::class,
			DelegateEnrollmentProofIntentUseCase::class,
			DismissAssistantIntentUseCase::class,
			RateAssistantMessageUseCase::class,
			ReportAssistantMessageUseCase::class,
			GetAssistantStatusUseCase::class,
			AppealAssistantRestrictionUseCase::class,
			AcceptAssistantConsentUseCase::class,
			GetAssistantMemoryUseCase::class,
			DeleteAssistantMemoryItemUseCase::class,
			ClearAssistantMemoryUseCase::class,
			AnswerStillAppliesUseCase::class,
			DeleteAssistantConversationsUseCase::class
		)
	}
}
