package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.usecase.AcceptAssistantConsentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AnswerStillAppliesUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AppealAssistantRestrictionUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AssistantIntentConfirmer
import com.gdavidpb.tuindice.assistant.domain.usecase.AssistantTurnStream
import com.gdavidpb.tuindice.assistant.domain.usecase.CancelAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmAddEvaluationsIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmCreatePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeleteEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeletePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditPlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmProjectionIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmRememberIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DelegateEnrollmentProofIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DismissAssistantIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ExtractEvaluationsFromPhotoUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.FollowAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantEvaluationCardsUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantMemoryUseCase
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
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatAccess
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatConversation
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatFeedback
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatIntents
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatNotificationOffer
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatPlanIntents
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatProposals
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatRestrictions
import com.gdavidpb.tuindice.assistant.presentation.machine.collaborator.AssistantChatTurns
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantEvaluationCardRepository
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConsentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantStandingRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantTurnRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingImageEncoderRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

// The chat machine over the fakes of the ports, with the real use cases and collaborators in between. The clock is
// fixed so the times the turns, the photos and the restrictions read are decided by the test.
internal class AssistantChatFixture(
	val notificationOffers: RecordingNotificationOfferRepository = RecordingNotificationOfferRepository()
) {
	val texts = FakeAssistantChatTexts()
	val feedbackTexts = FakeAssistantChatFeedbackTexts()
	val availability = ObservedAssistantAvailabilityRepository()
	val conversation = ObservedAssistantConversationRepository()
	val consent = RecordingAssistantConsentRepository()
	val notificationPermission = RecordingNotificationPermissionRepository(offers = notificationOffers)
	val turns = RecordingAssistantTurnRepository()
	val gatedTurns = GatedAssistantTurnRepository(turns)
	val intents = RecordingAssistantIntentRepository()
	val gatedIntents = GatedAssistantIntentRepository(intents)
	val evaluations = RecordingAssistantEvaluationsRepository()

	// The cards of the Evaluations tab the device has: none, unless a test sets some before the machine is built.
	var evaluationCards = FakeAssistantEvaluationCardRepository()
	val plans = RecordingAssistantPlansRepository()
	val projection = RecordingAssistantProjectionRepository()
	val feedback = RecordingAssistantFeedbackRepository()
	val standing = RecordingAssistantStandingRepository(
		configuredMode = { availability.availability.value.mode.name.lowercase() },
		consentGranted = { consent.isAssistantConsentGranted() }
	)
	val appeals = FailingAppealStandingRepository(standing)
	val memories = RecordingAssistantMemoryRepository()
	val gatedMemories = GatedAssistantMemoryRepository(memories)
	val serverDisabled = RecordingAssistantServerDisabledRepository()
	val clientState = FakeAssistantClientStateRepository()
	val imageEncoder = RecordingImageEncoderRepository()
	val extraction = RecordingAssistantExtractionRepository()
	val appEnvironment = FakeAppEnvironmentRepository()
	val effects = mutableListOf<AssistantChat.Effect>()

	private val reporting = RecordingReportingRepository()

	// Fixed, and moved by a test that lets the end of a restriction pass.
	val clock = FixedClock(nowMillis = NOW_MILLIS)

	val machine: AssistantChatMachine by lazy { createMachine() }

	// The dispatchers the sheet runs on: a sheet opened again runs on the same ones.
	private var sheetDispatchers: TuIndiceDispatchers? = null

	fun viewModel(dispatchers: TuIndiceDispatchers): AssistantChatViewModel {
		sheetDispatchers = dispatchers

		return AssistantChatViewModel(
			screenMachine = machine,
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	// A sheet opened again over the same conversation: a new machine, as a new sheet has, on the store the sheet before
	// left.
	fun reopenedViewModel(): AssistantChatViewModel {
		return AssistantChatViewModel(
			screenMachine = createMachine(),
			eventPublisher = NoOpEventPublisher,
			dispatchers = checkNotNull(sheetDispatchers)
		)
	}

	private fun createMachine(): AssistantChatMachine {
		val turnStream = AssistantTurnStream(
			turnRepository = turns,
			conversationRepository = conversation,
			serverDisabledRepository = serverDisabled
		)
		val confirmer = AssistantIntentConfirmer(
			conversationRepository = conversation,
			intentRepository = gatedIntents,
			outboxRepository = conversation,
			queuedMutationsRepository = RecordingAssistantQueuedMutationsRepository(),
			sessionCoroutineScope = testSessionCoroutineScope(),
			reportingRepository = reporting
		)

		return AssistantChatMachine(
			turns = createTurns(turnStream),
			proposals = AssistantChatProposals(
				intents = createIntents(confirmer),
				planIntents = createPlanIntents(confirmer)
			),
			feedback = createFeedback(),
			access = createAccess(confirmer),
			notificationOffer = AssistantChatNotificationOffer(
				permission = notificationPermission,
				offers = notificationOffers
			),
			texts = texts
		)
	}

	private fun createTurns(turnStream: AssistantTurnStream): AssistantChatTurns {
		return AssistantChatTurns(
			conversation = AssistantChatConversation(
				observeAssistantConversationUseCase = ObserveAssistantConversationUseCase(conversation, reporting),
				getAssistantEvaluationCardsUseCase = GetAssistantEvaluationCardsUseCase(evaluationCards, reporting)
			),
			resumeAssistantTurnUseCase = ResumeAssistantTurnUseCase(turnStream, conversation, reporting),
			followAssistantTurnUseCase = FollowAssistantTurnUseCase(turnStream, reporting),
			cancelAssistantTurnUseCase = CancelAssistantTurnUseCase(turns, reporting),
			sendAssistantMessageUseCase = SendAssistantMessageUseCase(
				conversationRepository = conversation,
				turnRepository = gatedTurns,
				clientStateRepository = clientState,
				clock = clock,
				reportingRepository = reporting,
				handling = SendAssistantMessageHandling(
					paramsValidator = SendAssistantMessageParamsValidator(),
					exceptionHandler = SendAssistantMessageExceptionHandler()
				)
			),
			extractEvaluationsFromPhotoUseCase = ExtractEvaluationsFromPhotoUseCase(
				imageEncoderRepository = imageEncoder,
				extractionRepository = extraction,
				conversationRepository = conversation,
				clock = clock,
				reportingRepository = reporting,
				exceptionHandler = ExtractEvaluationsFromPhotoExceptionHandler()
			)
		)
	}

	private fun createIntents(confirmer: AssistantIntentConfirmer): AssistantChatIntents {
		return AssistantChatIntents(
			dismissAssistantIntentUseCase = DismissAssistantIntentUseCase(
				gatedIntents,
				conversation,
				reporting,
				conversation,
				testSessionCoroutineScope()
			),
			delegateEnrollmentProofIntentUseCase = DelegateEnrollmentProofIntentUseCase(
				confirmer = confirmer,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmRememberIntentUseCase = ConfirmRememberIntentUseCase(
				confirmer = confirmer,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmAddEvaluationsIntentUseCase = ConfirmAddEvaluationsIntentUseCase(
				confirmer = confirmer,
				evaluationsRepository = evaluations,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmEditEvaluationIntentUseCase = ConfirmEditEvaluationIntentUseCase(
				confirmer = confirmer,
				evaluationsRepository = evaluations,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmDeleteEvaluationIntentUseCase = ConfirmDeleteEvaluationIntentUseCase(
				confirmer = confirmer,
				evaluationsRepository = evaluations,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			)
		)
	}

	private fun createPlanIntents(confirmer: AssistantIntentConfirmer): AssistantChatPlanIntents {
		return AssistantChatPlanIntents(
			confirmCreatePlanIntentUseCase = ConfirmCreatePlanIntentUseCase(
				confirmer = confirmer,
				plansRepository = plans,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmEditPlanIntentUseCase = ConfirmEditPlanIntentUseCase(
				confirmer = confirmer,
				plansRepository = plans,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmDeletePlanIntentUseCase = ConfirmDeletePlanIntentUseCase(
				confirmer = confirmer,
				plansRepository = plans,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			),
			confirmProjectionIntentUseCase = ConfirmProjectionIntentUseCase(
				confirmer = confirmer,
				projectionRepository = projection,
				reportingRepository = reporting,
				exceptionHandler = AssistantIntentExceptionHandler()
			)
		)
	}

	private fun createFeedback(): AssistantChatFeedback {
		return AssistantChatFeedback(
			rateAssistantMessageUseCase = RateAssistantMessageUseCase(feedback, reporting),
			reportAssistantMessageUseCase = ReportAssistantMessageUseCase(
				feedbackRepository = feedback,
				reportingRepository = reporting,
				paramsValidator = ReportAssistantMessageParamsValidator(),
				exceptionHandler = ReportAssistantMessageExceptionHandler()
			),
			answerStillAppliesUseCase = AnswerStillAppliesUseCase(gatedMemories, reporting),
			getAssistantMemoryUseCase = GetAssistantMemoryUseCase(gatedMemories, reporting),
			texts = feedbackTexts
		)
	}

	private fun createAccess(confirmer: AssistantIntentConfirmer): AssistantChatAccess {
		val syncAssistantUseCase = SyncAssistantUseCase(
			standingRepository = standing,
			serverModeRepository = availability,
			serverConsentRepository = consent,
			confirmer = confirmer,
			reportingRepository = reporting
		)

		return AssistantChatAccess(
			availabilityRepository = availability,
			consentRepository = consent,
			acceptAssistantConsentUseCase = AcceptAssistantConsentUseCase(consent, testSessionCoroutineScope(), reporting),
			syncAssistantUseCase = syncAssistantUseCase,
			appEnvironmentRepository = appEnvironment,
			restrictions = createRestrictions(syncAssistantUseCase)
		)
	}

	private fun createRestrictions(syncAssistantUseCase: SyncAssistantUseCase): AssistantChatRestrictions {
		return AssistantChatRestrictions(
			syncAssistantUseCase = syncAssistantUseCase,
			appealAssistantRestrictionUseCase = AppealAssistantRestrictionUseCase(
				standingRepository = appeals,
				reportingRepository = reporting,
				paramsValidator = AppealAssistantRestrictionParamsValidator(),
				exceptionHandler = AssistantAppealExceptionHandler()
			),
			feedbackTexts = feedbackTexts,
			clock = clock
		)
	}
}

// Runs [block] with a view model on the test dispatchers, whose state and effects are collected from the start.
@OptIn(ExperimentalCoroutinesApi::class)
internal fun chatTest(
	fixture: AssistantChatFixture = AssistantChatFixture(),
	block: suspend TestScope.(AssistantChatFixture, AssistantChatViewModel) -> Unit
) = runTest {
	withUnconfinedTestDispatchers { dispatchers ->
		val viewModel = fixture.viewModel(dispatchers)

		backgroundScope.launchStateCollector(viewModel.state, testScheduler)
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			viewModel.effect.collect { effect -> fixture.effects += effect }
		}

		block(fixture, viewModel)
	}
}

internal const val NOW_MILLIS = 1_700_000_000_000L
