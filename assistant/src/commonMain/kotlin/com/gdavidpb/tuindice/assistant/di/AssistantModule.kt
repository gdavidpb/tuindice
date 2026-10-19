package com.gdavidpb.tuindice.assistant.di

import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.data.source.AssistantAvailabilityDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConsentSettingsDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationStoreDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationsDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantInsightsDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantIntentOutboxDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantMemoryDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantTurnDataSource
import com.gdavidpb.tuindice.assistant.data.source.KtorAssistantApiDataSource
import com.gdavidpb.tuindice.assistant.data.source.SseAssistantEventsDataSource
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerConsentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
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
import com.gdavidpb.tuindice.assistant.domain.usecase.ExtractEvaluationsFromPhotoUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.FollowAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantStatusUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.RateAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ReportAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ResumeAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageHandling
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SyncAssistantUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantAppealExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ExtractEvaluationsFromPhotoExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ReportAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.SendAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.AppealAssistantRestrictionParamsValidator
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.ReportAssistantMessageParamsValidator
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.SendAssistantMessageParamsValidator
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatAccess
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatFeedback
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatIntents
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatNotificationOffer
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatPlanIntents
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatProposals
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatRestrictions
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatTurns
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatFeedbackResourceTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatResourceTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantMemoryResourceTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantMemoryTexts
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantMemoryViewModel
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val assistantModule = module {
	/* Data sources */

	factoryOf(::KtorAssistantApiDataSource) {
		bind<AssistantIntentRepository>()
		bind<AssistantFeedbackRepository>()
		bind<AssistantStandingRepository>()
		bind<AssistantExtractionRepository>()
	}
	factoryOf(::AssistantTurnDataSource) { bind<AssistantTurnRepository>() }
	factoryOf(::AssistantConversationsDataSource) { bind<AssistantConversationsRepository>() }
	single<AssistantEventsDataRepository> {
		SseAssistantEventsDataSource(
			httpClient = get<HttpClient>(named(ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER)),
			serverDisabled = get()
		)
	}
	singleOf(::AssistantAvailabilityDataSource) {
		bind<AssistantAvailabilityRepository>()
		bind<AssistantServerDisabledDataRepository>()
		bind<AssistantServerDisabledRepository>()
		bind<AssistantServerModeRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantConsentSettingsDataSource) {
		bind<AssistantConsentRepository>()
		bind<AssistantServerConsentRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantInsightsDataSource) {
		bind<AssistantInsightsRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantMemoryDataSource) { bind<AssistantMemoryRepository>() }
	singleOf(::AssistantConversationDataSource) { bind<SessionMemory>() }
	singleOf(::AssistantIntentOutboxDataSource) { bind<AssistantIntentOutboxRepository>() }
	singleOf(::AssistantConversationStoreDataSource) { bind<AssistantConversationRepository>() }

	/* Shared helpers and validators, exception handlers */

	factoryOf(::AssistantIntentConfirmer)
	factoryOf(::AssistantTurnStream)
	factoryOf(::AppealAssistantRestrictionParamsValidator)
	factoryOf(::AssistantAppealExceptionHandler)
	factoryOf(::ReportAssistantMessageParamsValidator)
	factoryOf(::ReportAssistantMessageExceptionHandler)
	factoryOf(::AssistantIntentExceptionHandler)
	factoryOf(::SendAssistantMessageParamsValidator)
	factoryOf(::SendAssistantMessageExceptionHandler)
	factoryOf(::SendAssistantMessageHandling)
	factoryOf(::ExtractEvaluationsFromPhotoExceptionHandler)

	/* Use cases */

	factoryOf(::ObserveAssistantConversationUseCase)
	factoryOf(::SendAssistantMessageUseCase)
	factoryOf(::ExtractEvaluationsFromPhotoUseCase)
	factoryOf(::FollowAssistantTurnUseCase)
	factoryOf(::ResumeAssistantTurnUseCase)
	factoryOf(::CancelAssistantTurnUseCase)
	factoryOf(::ConfirmAddEvaluationsIntentUseCase)
	factoryOf(::ConfirmEditEvaluationIntentUseCase)
	factoryOf(::ConfirmDeleteEvaluationIntentUseCase)
	factoryOf(::ConfirmCreatePlanIntentUseCase)
	factoryOf(::ConfirmEditPlanIntentUseCase)
	factoryOf(::ConfirmDeletePlanIntentUseCase)
	factoryOf(::ConfirmProjectionIntentUseCase)
	factoryOf(::ConfirmRememberIntentUseCase)
	factoryOf(::DelegateEnrollmentProofIntentUseCase)
	factoryOf(::DismissAssistantIntentUseCase)
	factoryOf(::RateAssistantMessageUseCase)
	factoryOf(::ReportAssistantMessageUseCase)
	factoryOf(::GetAssistantStatusUseCase)
	factoryOf(::SyncAssistantUseCase)
	factoryOf(::AppealAssistantRestrictionUseCase)
	factoryOf(::AcceptAssistantConsentUseCase)
	factoryOf(::GetAssistantMemoryUseCase)
	factoryOf(::DeleteAssistantMemoryItemUseCase)
	factoryOf(::ClearAssistantMemoryUseCase)
	factoryOf(::AnswerStillAppliesUseCase)
	factoryOf(::DeleteAssistantConversationsUseCase)

	/* Chat presentation: the sheet's machine and its collaborators, its texts, and its view model */

	viewModelOf(::AssistantChatViewModel)
	factoryOf(::AssistantChatMachine)
	factoryOf(::AssistantChatTurns)
	factoryOf(::AssistantChatIntents)
	factoryOf(::AssistantChatPlanIntents)
	factoryOf(::AssistantChatProposals)
	factoryOf(::AssistantChatFeedback)
	factoryOf(::AssistantChatAccess)
	factoryOf(::AssistantChatRestrictions)
	factoryOf(::AssistantChatNotificationOffer)
	factoryOf(::AssistantChatResourceTexts) { bind<AssistantChatTexts>() }
	factoryOf(::AssistantChatFeedbackResourceTexts) { bind<AssistantChatFeedbackTexts>() }

	/* Memory presentation: the screen's machine, its texts and its view model */

	viewModelOf(::AssistantMemoryViewModel)
	factoryOf(::AssistantMemoryMachine)
	factoryOf(::AssistantMemoryResourceTexts) { bind<AssistantMemoryTexts>() }

	/* Component sheet presentation: a read-only view of a component of the conversation */

	viewModelOf(::AssistantComponentSheetViewModel)
	factoryOf(::AssistantComponentSheetMachine)
}
