package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.mapper.toIntentExecution
import com.gdavidpb.tuindice.assistant.domain.model.DETAIL_PENSUM_NOT_LOADED
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.model.IntentResolvedCount
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.testing.FAR_FUTURE_MILLIS
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantIntentConfirmationTest {
	private val calls = mutableListOf<String>()
	private val intents = RecordingAssistantIntentRepository(calls)
	private val conversation = RecordingAssistantConversationRepository(calls)
	private val evaluations = RecordingAssistantEvaluationsRepository(calls)
	private val plans = RecordingAssistantPlansRepository(calls)
	private val projections = RecordingAssistantProjectionRepository(calls)
	private val reporting = RecordingReportingRepository()
	private val queued = RecordingAssistantQueuedMutationsRepository(calls)
	private val sessionScope = testSessionCoroutineScope()
	private val confirmer = AssistantIntentConfirmer(
		conversationRepository = conversation,
		intentRepository = intents,
		outboxRepository = conversation,
		queuedMutationsRepository = queued,
		sessionCoroutineScope = sessionScope,
		reportingRepository = reporting
	)

	private fun dismiss() = DismissAssistantIntentUseCase(intents, conversation, reporting, conversation, sessionScope)
	private val handler = AssistantIntentExceptionHandler()

	private fun openIntent(intent: Intent) {
		conversation.openIntents = mapOf(intent.id to intent)
	}

	private suspend fun confirmEdit(intentId: String = "i1", editedArgs: IntentArgs? = null) =
		ConfirmEditEvaluationIntentUseCase(
			confirmer = confirmer,
			evaluationsRepository = evaluations,
			reportingRepository = reporting,
			exceptionHandler = handler
		).execute(ConfirmIntentParams(intentId = intentId, editedArgs = editedArgs)).lastState()

	@Test
	fun aConfirmedIntentRunsOnItsPortAndItsOutcomeIsReportedWithTheMutationsOfItsBinding() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED, detailCode = "ok", newRevision = 8)

		val state = confirmEdit()

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), state)
		assertEquals(listOf<Any>(args), evaluations.received)
		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertEquals(
			listOf(
				IntentOutcomeReport(
					intentId = "i1",
					outcome = OutcomeKind.CONFIRMED,
					mutationIds = listOf("m1"),
					detailCode = "ok",
					newRevision = 8
				)
			),
			intents.reports
		)
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
		assertEquals(listOf("decide", "port", "reportOutcome", "markIntentResolved"), calls)
	}

	@Test
	fun editedArgsOfTheSameKindReplaceTheProposedOnes() = runTest {
		val proposed = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 10.0)
		val edited = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 15.0)
		openIntent(assistantIntent(args = proposed))

		confirmEdit(editedArgs = edited)

		assertEquals(listOf<Any>(edited), evaluations.received)
		assertEquals(edited, intents.decisions.single().editedArgs)
	}

	@Test
	fun editedArgsOfAnotherKindAreNotRunAndTheProposedOnesStay() = runTest {
		val proposed = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 10.0)
		openIntent(assistantIntent(args = proposed))

		confirmEdit(editedArgs = RemoveEvaluationArgs(mutationId = "m2", evaluationId = "e2"))

		assertEquals(listOf<Any>(proposed), evaluations.received)
		assertEquals(null, intents.decisions.single().editedArgs)
	}

	@Test
	fun aBatchReportsOnlyTheMutationsOfTheRowsLeftMarked() = runTest {
		val proposed = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2"), draft("m3")))
		val edited = proposed.copy(items = proposed.items.map { item -> item.copy(selected = item.mutationId != "m2") })
		openIntent(assistantIntent(args = proposed, mutationIds = listOf("m1", "m2", "m3")))

		ConfirmAddEvaluationsIntentUseCase(
			confirmer = confirmer,
			evaluationsRepository = evaluations,
			reportingRepository = reporting,
			exceptionHandler = handler
		).execute(ConfirmIntentParams(intentId = "i1", editedArgs = edited)).lastState()

		assertEquals(listOf<Any>(edited), evaluations.received)
		assertEquals(listOf("m1", "m3"), intents.reports.single().mutationIds)
	}

	private fun draft(mutationId: String) = EvaluationDraft(
		mutationId = mutationId,
		reference = "Parcial",
		attemptId = "a1",
		subjectCode = "CI4325",
		termId = "t1",
		evaluationType = "QUIZ",
		scheduleMode = "dated",
		maxGrade = 20.0
	)

	@Test
	fun aReportTheServerDoesNotTakeStaysPending_andIsSentByTheNextFlush() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
		intents.reportError = IllegalStateException("offline")

		val state = confirmEdit()

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), state)
		assertEquals(OutcomeKind.CONFIRMED, conversation.resolved["i1"])
		assertEquals(listOf("i1"), conversation.pendingOutcomes().map { report -> report.intentId })

		intents.reportError = null
		confirmer.flushPendingOutcomes()

		assertEquals(listOf("i1"), intents.reports.map { report -> report.intentId })
		assertEquals(OutcomeKind.CONFIRMED, intents.reports.single().outcome)
		assertTrue(conversation.pendingOutcomes().isEmpty())
	}

	@Test
	fun aDecisionWhoseAnswerWasLost_runsWhenTheStudentRepeatsIt_asTheServerAnswersItDecided() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
		intents.decideError = IllegalStateException("lost in the network")
		assertTrue(confirmEdit() is UseCaseState.Error)

		// The server applied the first decision: its repetition is answered as decided, and the intent runs.
		intents.decideError = null
		intents.answer = IntentDecisionResult.Accepted

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())
		assertEquals(listOf<Any>(args), evaluations.received)
	}

	@Test
	fun aConflictOfARepeatedDecisionIsFinal_andNothingRuns() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
		intents.decideError = IllegalStateException("lost in the network")
		assertTrue(confirmEdit() is UseCaseState.Error)

		// A conflict answers a different decision made since: the intent is closed as the conflict it is.
		intents.decideError = null
		intents.answer = IntentDecisionResult.Conflict

		assertEquals(UseCaseState.Data(OutcomeKind.CONFLICT), confirmEdit())
		assertTrue(evaluations.received.isEmpty())
	}

	// The deadline is the server's to judge: an intent whose deadline the phone's clock has passed is still decided by the
	// server, and it runs when the server takes it.
	@Test
	fun anIntentPastItsDeadlineOnThePhone_isStillDecidedByTheServer() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")
		openIntent(assistantIntent(args = args, expiresAtMillis = NOW_MILLIS - 1))

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertEquals(listOf<Any>(args), evaluations.received)
	}

	@Test
	fun anIntentTheServerNoLongerTakesIsReportedExpiredWithoutRunning() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		intents.answer = IntentDecisionResult.Expired

		assertEquals(UseCaseState.Data(OutcomeKind.EXPIRED), confirmEdit())

		assertTrue(evaluations.received.isEmpty())
		assertEquals(listOf(IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.EXPIRED)), intents.reports)
		assertEquals(mapOf("i1" to OutcomeKind.EXPIRED), conversation.resolved)
	}

	@Test
	fun aConflictOnTheServerIsReportedAsConflictWithoutRunning() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		intents.answer = IntentDecisionResult.Conflict

		assertEquals(UseCaseState.Data(OutcomeKind.CONFLICT), confirmEdit())

		assertTrue(evaluations.received.isEmpty())
		assertEquals(listOf(IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.CONFLICT)), intents.reports)
		assertEquals(mapOf("i1" to OutcomeKind.CONFLICT), conversation.resolved)
	}

	@Test
	fun aPortThatFailsAnswersFailedAndTheFailureIsReported() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.FAILED, detailCode = "db_locked")

		assertEquals(UseCaseState.Data(OutcomeKind.FAILED), confirmEdit())

		assertEquals(
			listOf(
				IntentOutcomeReport(
					intentId = "i1",
					outcome = OutcomeKind.FAILED,
					mutationIds = listOf("m1"),
					detailCode = "db_locked"
				)
			),
			intents.reports
		)
		assertEquals(mapOf("i1" to OutcomeKind.FAILED), conversation.resolved)
	}

	@Test
	fun aPlanRefusedWhileThePensumIsNotLoaded_keepsItsDetailWithTheIntentItResolves() = runTest {
		val args = CreatePlanArgs(mutationId = "m2", periodYear = 2026, periodCode = "1", subjectCodes = listOf("CI5311"))
		openIntent(assistantIntent(args = args))
		plans.answer = IntentExecution(outcome = OutcomeKind.REJECTED, detailCode = DETAIL_PENSUM_NOT_LOADED)

		val state = ConfirmCreatePlanIntentUseCase(confirmer, plans, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(UseCaseState.Data(OutcomeKind.REJECTED), state)
		assertEquals(mapOf("i1" to OutcomeKind.REJECTED), conversation.resolved)
		assertEquals(mapOf("i1" to DETAIL_PENSUM_NOT_LOADED), conversation.resolvedDetails)
	}

	@Test
	fun aPortThatThrowsIsNotAnOutcomeAndNothingIsReported() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		evaluations.error = IllegalStateException("boom")

		assertEquals(UseCaseState.Error<AssistantIntentError>(null), confirmEdit())

		assertTrue(intents.reports.isEmpty())
		assertTrue(conversation.resolved.isEmpty())
	}

	@Test
	fun anIntentThatIsNotOpenIsAnErrorAndNothingIsDecided() = runTest {
		assertEquals(UseCaseState.Error(AssistantIntentError.IntentNotOpen), confirmEdit())

		assertTrue(intents.decisions.isEmpty())
	}

	@Test
	fun anIntentOfAnotherKindIsAnErrorAndNothingIsDecided() = runTest {
		openIntent(assistantIntent(args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")))

		assertEquals(UseCaseState.Error(AssistantIntentError.IntentKindMismatch), confirmEdit())

		assertTrue(intents.decisions.isEmpty())
		assertTrue(evaluations.received.isEmpty())
	}

	@Test
	fun aPlanConfirmationRunsWithTheRevisionTheIntentWasBuiltOn() = runTest {
		val args = CreatePlanArgs(mutationId = "m2", periodYear = 2026, periodCode = "1", subjectCodes = listOf("CI5311"))
		openIntent(assistantIntent(args = args, expectedRevision = 7L))

		ConfirmCreatePlanIntentUseCase(confirmer, plans, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(listOf<Pair<Any, Long?>>(args to 7L), plans.received)
	}

	@Test
	fun aProjectionConfirmationRunsWithTheRevisionTheIntentWasBuiltOn() = runTest {
		val args = ProjectionArgs(mutationId = "m3", attemptId = "a1", grade = 12, outcome = "approved")
		openIntent(assistantIntent(args = args, expectedRevision = 9L))

		ConfirmProjectionIntentUseCase(confirmer, projections, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(listOf<Pair<ProjectionArgs, Long?>>(args to 9L), projections.received)
	}

	@Test
	fun aRememberedMemoryIsOnlyDecidedAndClosedAsConfirmedWithoutAReport() = runTest {
		val args = RememberArgs(statement = "Estudia de noche", category = "habitos", quote = "estudio de noche")
		openIntent(assistantIntent(args = args, mutationIds = emptyList()))

		val state = ConfirmRememberIntentUseCase(confirmer, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), state)
		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertTrue(intents.reports.isEmpty())
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
		assertEquals(listOf("decide", "markIntentResolved"), calls)
	}

	@Test
	fun aDelegatedEnrollmentProofIsReportedAsDelegated() = runTest {
		openIntent(assistantIntent(args = RequestEnrollmentProofArgs, mutationIds = emptyList()))

		val state = DelegateEnrollmentProofIntentUseCase(confirmer, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(UseCaseState.Data(OutcomeKind.DELEGATED), state)
		assertEquals(listOf(IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.DELEGATED)), intents.reports)
		assertEquals(mapOf("i1" to OutcomeKind.DELEGATED), conversation.resolved)
	}

	@Test
	fun aDismissedIntentIsDeclinedAndClosedAsCancelledWithoutAnOutcomeReport() = runTest {
		openIntent(assistantIntent(args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")))

		dismiss().execute("i1").dataValues()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = false)), intents.decisions)
		assertTrue(intents.reports.isEmpty())
		assertEquals(mapOf("i1" to OutcomeKind.CANCELLED), conversation.resolved)
		assertTrue(evaluations.received.isEmpty())
	}

	@Test
	fun anEditOrARemovalRunsWithTheRevisionOfTheEvaluationTheIntentWasBuiltOn() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1"), expectedRevision = 4L))
		confirmEdit()

		val removal = RemoveEvaluationArgs(mutationId = "m2", evaluationId = "e1")
		openIntent(assistantIntent(args = removal, expectedRevision = 5L))
		ConfirmDeleteEvaluationIntentUseCase(confirmer, evaluations, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(listOf<Long?>(4L, 5L), evaluations.expectedRevisions)
	}

	@Test
	fun aBatchReportsHowEachOfItsMutationsEnded() = runTest {
		val items = listOf(
			MutationOutcome("m1", OutcomeKind.CONFIRMED),
			MutationOutcome("m2", OutcomeKind.FAILED, detailCode = "unhandled")
		)
		val batch = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2")))
		openIntent(assistantIntent(args = batch, mutationIds = listOf("m1", "m2")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED, itemOutcomes = items)

		confirmAdd()

		assertEquals(items, intents.reports.single().itemOutcomes)
		assertTrue(queued.followed.isEmpty())
	}

	@Test
	fun aQueuedBatchIsFollowed_andTheOutcomeItsMutationsSettleWithIsReportedAndResolvesTheIntent() = runTest {
		openIntent(
			assistantIntent(
				args = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2"), draft("m3"))),
				mutationIds = listOf("m1", "m2", "m3"),
				expiresAtMillis = NOW_MILLIS + 60_000L
			)
		)
		evaluations.answer = IntentExecution(
			outcome = OutcomeKind.QUEUED,
			itemOutcomes = listOf(
				MutationOutcome("m1", OutcomeKind.CONFIRMED),
				MutationOutcome("m2", OutcomeKind.QUEUED),
				MutationOutcome("m3", OutcomeKind.QUEUED)
			)
		)

		val state = confirmAdd()

		assertEquals(UseCaseState.Data(OutcomeKind.QUEUED), state)
		assertEquals(listOf(listOf("m2", "m3") to NOW_MILLIS + 60_000L), queued.followed)
		assertEquals(mapOf("i1" to OutcomeKind.QUEUED), conversation.resolved)

		// m2 lands and m3 fails after the intent answered: the batch ends confirmed, with each mutation's outcome.
		queued.settle(
			listOf(MutationOutcome("m2", OutcomeKind.CONFIRMED), MutationOutcome("m3", OutcomeKind.FAILED, "unhandled"))
		)

		val final = intents.reports.last()
		assertEquals(OutcomeKind.CONFIRMED, final.outcome)
		assertEquals(listOf("m1", "m2", "m3"), final.mutationIds)
		assertEquals(
			listOf(
				MutationOutcome("m1", OutcomeKind.CONFIRMED),
				MutationOutcome("m2", OutcomeKind.CONFIRMED),
				MutationOutcome("m3", OutcomeKind.FAILED, "unhandled")
			),
			final.itemOutcomes
		)
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
		assertEquals(IntentResolvedCount(applied = 2, selected = 3), conversation.resolvedCounts["i1"])
		assertTrue(conversation.pendingOutcomes().isEmpty())
	}

	@Test
	fun aQueuedSingleMutationIsFollowedByItsIdAndEndsWithTheOutcomeItSettlesWith() = runTest {
		val edit = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")
		openIntent(assistantIntent(args = edit, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.QUEUED)

		confirmEdit()
		queued.settle(listOf(MutationOutcome("m1", OutcomeKind.CONFLICT, "Revision")))

		assertEquals(listOf(listOf("m1") to FAR_FUTURE_MILLIS), queued.followed)
		assertEquals(
			IntentOutcomeReport(
				intentId = "i1",
				outcome = OutcomeKind.CONFLICT,
				mutationIds = listOf("m1"),
				detailCode = "Revision"
			),
			intents.reports.last()
		)
		assertEquals(mapOf("i1" to OutcomeKind.CONFLICT), conversation.resolved)
	}

	@Test
	fun anAdditionThatEndedWithoutAnyRowApplied_keepsNoCount() = runTest {
		for (outcome in listOf(OutcomeKind.CONFLICT, OutcomeKind.EXPIRED, OutcomeKind.REJECTED)) {
			openIntent(assistantIntent(args = AddEvaluationsArgs(items = listOf(draft("m1")))))
			evaluations.answer = IntentExecution(outcome = outcome)

			confirmAdd()

			assertEquals(outcome, conversation.resolved["i1"])
			assertNull(conversation.resolvedCounts["i1"])
		}
	}

	@Test
	fun aBatchWhoseRowsAllFailed_keepsNoneApplied_andItsOutcomeComesFromTheRowsMapper() = runTest {
		val batch = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2"), draft("m3")))
		openIntent(assistantIntent(args = batch, mutationIds = listOf("m1", "m2", "m3")))
		evaluations.answer = checkNotNull(
			listOf(
				MutationOutcome("m1", OutcomeKind.FAILED, detailCode = "unhandled"),
				MutationOutcome("m2", OutcomeKind.FAILED, detailCode = "unhandled"),
				MutationOutcome("m3", OutcomeKind.FAILED, detailCode = "unhandled")
			).toIntentExecution()
		)

		val state = confirmAdd()

		assertEquals(UseCaseState.Data(OutcomeKind.FAILED), state)
		assertEquals(OutcomeKind.FAILED, conversation.resolved["i1"])
		assertEquals(IntentResolvedCount(applied = 0, selected = 3), conversation.resolvedCounts["i1"])
	}

	@Test
	fun aSingleRowQueuedAndSettledConfirmed_countsOneAppliedOfOneMarked() = runTest {
		openIntent(assistantIntent(args = AddEvaluationsArgs(items = listOf(draft("m1")))))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.QUEUED)

		confirmAdd()
		queued.settle(listOf(MutationOutcome("m1", OutcomeKind.CONFIRMED)))

		assertEquals(OutcomeKind.CONFIRMED, conversation.resolved["i1"])
		assertEquals(IntentResolvedCount(applied = 1, selected = 1), conversation.resolvedCounts["i1"])
	}

	@Test
	fun aQueueSettledOnlyInPart_staysQueued_andItsCountIsProvisionalUntilTheRestSettles() = runTest {
		val batch = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2"), draft("m3")))
		openIntent(
			assistantIntent(
				args = batch,
				mutationIds = listOf("m1", "m2", "m3"),
				expiresAtMillis = NOW_MILLIS + 60_000L
			)
		)
		evaluations.answer = IntentExecution(
			outcome = OutcomeKind.QUEUED,
			itemOutcomes = listOf(
				MutationOutcome("m1", OutcomeKind.CONFIRMED),
				MutationOutcome("m2", OutcomeKind.QUEUED),
				MutationOutcome("m3", OutcomeKind.QUEUED)
			)
		)

		confirmAdd()
		assertEquals(IntentResolvedCount(applied = 1, selected = 3), conversation.resolvedCounts["i1"])

		queued.settle(listOf(MutationOutcome("m2", OutcomeKind.CONFIRMED)))

		assertEquals(OutcomeKind.QUEUED, conversation.resolved["i1"])
		assertEquals(IntentResolvedCount(applied = 2, selected = 3), conversation.resolvedCounts["i1"])
	}

	private suspend fun confirmAdd(editedArgs: IntentArgs? = null) = ConfirmAddEvaluationsIntentUseCase(
		confirmer = confirmer,
		evaluationsRepository = evaluations,
		reportingRepository = reporting,
		exceptionHandler = handler
	).execute(ConfirmIntentParams(intentId = "i1", editedArgs = editedArgs)).lastState()

	@Test
	fun aBatchWithOneRowMarkedOutOfTwoKeepsTheMarkedAndTheAppliedCount() = runTest {
		val proposed = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2")))
		val edited = proposed.copy(items = proposed.items.map { item -> item.copy(selected = item.mutationId == "m1") })
		openIntent(assistantIntent(args = proposed, mutationIds = listOf("m1", "m2")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)

		confirmAdd(editedArgs = edited)

		assertEquals(IntentResolvedCount(applied = 1, selected = 1), conversation.resolvedCounts["i1"])
	}

	@Test
	fun aBatchWithAFailedRowKeepsHowManyOfItsMarkedRowsLanded() = runTest {
		val batch = AddEvaluationsArgs(items = listOf(draft("m1"), draft("m2"), draft("m3")))
		openIntent(assistantIntent(args = batch, mutationIds = listOf("m1", "m2", "m3")))
		evaluations.answer = IntentExecution(
			outcome = OutcomeKind.CONFIRMED,
			itemOutcomes = listOf(
				MutationOutcome("m1", OutcomeKind.CONFIRMED),
				MutationOutcome("m2", OutcomeKind.FAILED, detailCode = "unhandled"),
				MutationOutcome("m3", OutcomeKind.CONFIRMED)
			)
		)

		confirmAdd()

		assertEquals(IntentResolvedCount(applied = 2, selected = 3), conversation.resolvedCounts["i1"])
	}

	@Test
	fun aRemovalKeepsNoCount_becauseOnlyAnAdditionReceiptsWhatItAdded() = runTest {
		openIntent(assistantIntent(args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")))

		ConfirmDeleteEvaluationIntentUseCase(confirmer, evaluations, reporting, handler)
			.execute(ConfirmIntentParams(intentId = "i1"))
			.lastState()

		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
		assertTrue(conversation.resolvedCounts.isEmpty())
	}

	@Test
	fun aDismissTheServerAnswersExpiredClosesTheIntentAsExpiredNotAsCancelled() = runTest {
		openIntent(assistantIntent(args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		intents.answer = IntentDecisionResult.Expired

		dismiss().execute("i1").dataValues()

		assertEquals(mapOf("i1" to OutcomeKind.EXPIRED), conversation.resolved)
		assertTrue(intents.reports.isEmpty())
	}

	@Test
	fun aDismissTheServerAnswersInConflictClosesTheIntentAsConflictNotAsCancelled() = runTest {
		openIntent(assistantIntent(args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		intents.answer = IntentDecisionResult.Conflict

		dismiss().execute("i1").dataValues()

		assertEquals(mapOf("i1" to OutcomeKind.CONFLICT), conversation.resolved)
	}

	// The session ends while the server decides: the intent is not run, and nothing is written for the account that left.
	@Test
	fun aSessionThatEndsWhileTheServerDecidesRunsNothingAndWritesNothing() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
		intents.onDecide = { conversation.endSession() }

		val state = confirmEdit()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertEquals(UseCaseState.Data(OutcomeKind.CANCELLED), state)
		assertTrue(evaluations.received.isEmpty())
		assertTrue(queued.followed.isEmpty())
		assertTrue(intents.reports.isEmpty())
		assertTrue(conversation.resolved.isEmpty())
	}

	// A run that fails answers nothing: the intent stays open, and a confirmation after the failure runs it.
	@Test
	fun aConfirmationWhoseRunFailsLeavesTheIntentToBeConfirmedAgain() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		evaluations.error = IllegalStateException("boom")

		assertEquals(UseCaseState.Error<AssistantIntentError>(null), confirmEdit())

		evaluations.error = null
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())
		assertEquals(2, evaluations.received.size)
	}

	// The sheet closes while the evaluation waits for its outcome, and the intent is confirmed again while the
	// first run still waits: the run goes on for the session, and the second confirmation finds the intent
	// taken. One run, one report.
	@Test
	fun aConfirmationTheSheetLeavesWhileItWaitsRunsTheIntentOnlyOnce() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		val release = CompletableDeferred<Unit>()
		val port = GatedEvaluations(gate = release)
		val useCase = ConfirmEditEvaluationIntentUseCase(confirmer, port, reporting, handler)
		val sheet = launch { useCase.execute(ConfirmIntentParams(intentId = "i1")).lastState() }
		runCurrent()

		sheet.cancelAndJoin()
		port.gate = null
		val reopened = useCase.execute(ConfirmIntentParams(intentId = "i1")).lastState()

		assertEquals(1, port.calls)
		assertEquals(UseCaseState.Error(AssistantIntentError.IntentNotOpen), reopened)
		assertEquals(1, intents.decisions.size)

		release.complete(Unit)

		assertEquals(listOf(OutcomeKind.CONFIRMED), intents.reports.map { report -> report.outcome })
		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
	}

	// Discarding an intent while its confirmation runs must not close it: the run holds the intent, and the server
	// has already taken the accept, so the decline never reaches it.
	@Test
	fun aDiscardWhileTheConfirmationRunsDoesNotCloseTheIntent() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		val release = CompletableDeferred<Unit>()
		val port = GatedEvaluations(gate = release)
		val useCase = ConfirmEditEvaluationIntentUseCase(confirmer, port, reporting, handler)
		val sheet = launch { useCase.execute(ConfirmIntentParams(intentId = "i1")).lastState() }
		runCurrent()

		dismiss().execute("i1").dataValues()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = true)), intents.decisions)
		assertEquals(emptyMap<String, OutcomeKind>(), conversation.resolved)

		release.complete(Unit)
		sheet.join()

		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), conversation.resolved)
	}

	// A confirmation that comes while a discard goes on is refused: the discard holds the intent, and it closes it.
	@Test
	fun aConfirmationWhileADiscardGoesOnIsRefusedAndTheDiscardClosesTheIntent() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		var refused: UseCaseState<*, *>? = null
		intents.onDecide = {
			intents.onDecide = {}
			refused = confirmEdit()
		}

		dismiss().execute("i1").dataValues()

		assertEquals(UseCaseState.Error(AssistantIntentError.IntentNotOpen), refused)
		assertTrue(evaluations.received.isEmpty())
		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = false)), intents.decisions)
		assertEquals(mapOf("i1" to OutcomeKind.CANCELLED), conversation.resolved)
	}

	// A discard that fails answers nothing and leaves the intent open: its claim goes with it, so a confirmation runs it.
	@Test
	fun aDiscardThatFailsLeavesTheIntentToBeConfirmedAgain() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		intents.decideError = IllegalStateException("offline")

		val dismissed = dismiss()
			.execute("i1")
			.lastState()

		assertTrue(dismissed is UseCaseState.Error<*>)

		intents.decideError = null
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())
		assertEquals(1, evaluations.received.size)
	}

	// Leaving the screen in the middle of a discard does not stop it: the decline is sent, the intent closes, and the
	// claim goes with the run.
	@Test
	fun aDiscardLeftInTheMiddleStillDeclinesClosesTheIntentAndReleasesIt() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		val answered = CompletableDeferred<Unit>()
		intents.onDecide = { answered.await() }
		val screen = launch { dismiss().execute("i1").lastState() }
		runCurrent()

		screen.cancelAndJoin()
		answered.complete(Unit)

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = false)), intents.decisions)
		assertEquals(mapOf("i1" to OutcomeKind.CANCELLED), conversation.resolved)
		assertTrue(conversation.confirming.isEmpty())
	}

	// A discard left in the middle that then fails releases its claim: the intent stays open, and a confirmation runs it.
	@Test
	fun aDiscardLeftInTheMiddleThatFailsReleasesTheIntentToBeConfirmed() = runTest {
		openIntent(assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1")))
		val answered = CompletableDeferred<Unit>()
		intents.onDecide = {
			answered.await()
			error("lost")
		}
		val screen = launch { dismiss().execute("i1").lastState() }
		runCurrent()

		screen.cancelAndJoin()
		answered.complete(Unit)

		assertTrue(conversation.confirming.isEmpty())
		assertEquals(emptyMap<String, OutcomeKind>(), conversation.resolved)

		intents.onDecide = {}
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())
	}

	// A port whose calls wait for the gate while it is set, as a mutation waits for its outcome.
	private class GatedEvaluations(var gate: CompletableDeferred<Unit>?) : AssistantEvaluationsRepository {
		var calls = 0

		override suspend fun add(args: AddEvaluationsArgs): IntentExecution = settle()

		override suspend fun edit(args: EditEvaluationArgs, expectedRevision: Long?): IntentExecution = settle()

		override suspend fun remove(args: RemoveEvaluationArgs, expectedRevision: Long?): IntentExecution = settle()

		private suspend fun settle(): IntentExecution {
			calls++
			gate?.await()

			return IntentExecution(outcome = OutcomeKind.CONFIRMED)
		}
	}

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
