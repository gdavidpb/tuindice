package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantIntentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantPlansRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantProjectionRepository
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
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantIntentConfirmationTest {
	private val calls = mutableListOf<String>()
	private val clock = FixedClock(nowMillis = NOW_MILLIS)
	private val intents = RecordingAssistantIntentRepository(calls)
	private val conversation = RecordingAssistantConversationRepository(calls)
	private val evaluations = RecordingAssistantEvaluationsRepository(calls)
	private val plans = RecordingAssistantPlansRepository(calls)
	private val projections = RecordingAssistantProjectionRepository(calls)
	private val reporting = RecordingReportingRepository()
	private val confirmer = AssistantIntentConfirmer(conversation, intents, clock)
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
		FlushAssistantOutcomesUseCase(confirmer, reporting).execute(Unit).lastState()

		assertEquals(listOf("i1"), intents.reports.map { report -> report.intentId })
		assertEquals(OutcomeKind.CONFIRMED, intents.reports.single().outcome)
		assertTrue(conversation.pendingOutcomes().isEmpty())
	}

	@Test
	fun aDecisionWhoseAnswerWasLost_isNotClosedAsConflictWhenTheStudentRepeatsIt() = runTest {
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 14.0)
		openIntent(assistantIntent(args = args, mutationIds = listOf("m1")))
		evaluations.answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
		intents.decideError = IllegalStateException("lost in the network")
		assertTrue(confirmEdit() is UseCaseState.Error)

		// The server applied the first decision: its repetition answers Conflict, and the intent runs as decided.
		intents.decideError = null
		intents.answer = IntentDecisionResult.Conflict

		assertEquals(UseCaseState.Data(OutcomeKind.CONFIRMED), confirmEdit())
		assertEquals(listOf<Any>(args), evaluations.received)
	}

	@Test
	fun anIntentPastItsDeadlineIsReportedExpiredWithoutDecidingOrRunning() = runTest {
		openIntent(
			assistantIntent(
				args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1"),
				expiresAtMillis = NOW_MILLIS - 1
			)
		)

		assertEquals(UseCaseState.Data(OutcomeKind.EXPIRED), confirmEdit())

		assertTrue(intents.decisions.isEmpty())
		assertTrue(evaluations.received.isEmpty())
		assertEquals(listOf(IntentOutcomeReport(intentId = "i1", outcome = OutcomeKind.EXPIRED)), intents.reports)
		assertEquals(mapOf("i1" to OutcomeKind.EXPIRED), conversation.resolved)
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

		DismissAssistantIntentUseCase(intents, conversation, reporting).execute("i1").dataValues()

		assertEquals(listOf(IntentDecision(intentId = "i1", accepted = false)), intents.decisions)
		assertTrue(intents.reports.isEmpty())
		assertEquals(mapOf("i1" to OutcomeKind.CANCELLED), conversation.resolved)
		assertTrue(evaluations.received.isEmpty())
	}

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
