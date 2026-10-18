package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.ObserveEvaluationMutationOutcomesUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome as ChatMutationOutcome

@OptIn(ExperimentalCoroutinesApi::class)
class EvaluationsAssistantDataSourceTest {
	@Test
	fun add_subscribesToTheOutcomesBeforeAnyMutationRuns() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2")))

		assertEquals(listOf("m1", "m2"), repository.mutations)
		assertEquals(listOf(true, true), repository.subscribedAtMutation)
		assertEquals(
			IntentExecution(
				OutcomeKind.CONFIRMED,
				itemOutcomes = listOf(item("m1", OutcomeKind.CONFIRMED), item("m2", OutcomeKind.CONFIRMED))
			),
			execution
		)
	}

	@Test
	fun add_answersTheOutcomeOfAnItem() = runTest {
		assertAddAnswer(MutationOutcomeKind.CONFIRMED, IntentExecution(OutcomeKind.CONFIRMED))
		assertAddAnswer(MutationOutcomeKind.CONFLICT, IntentExecution(OutcomeKind.CONFLICT))
		assertAddAnswer(MutationOutcomeKind.REJECTED, IntentExecution(OutcomeKind.REJECTED))
		assertAddAnswer(MutationOutcomeKind.SUPERSEDED, IntentExecution(OutcomeKind.FAILED, "SUPERSEDED"))
		assertAddAnswer(MutationOutcomeKind.CANCELLED, IntentExecution(OutcomeKind.FAILED, "CANCELLED"))
	}

	@Test
	fun add_carriesTheDetailCodeOfAnOutcomeThatHasOne() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(MutationOutcomeKind.REJECTED, "bad_grade"))

		val execution = evaluationsSource(repository).add(addArgs(draft("m1")))

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "bad_grade"), execution)
	}

	@Test
	fun add_aBatchThatLandedSomeItemsIsConfirmedAndNamesHowEachOneEnded() = runTest {
		val outcomes = mapOf(
			"m1" to MutationOutcomeKind.CONFIRMED,
			"m2" to MutationOutcomeKind.CONFLICT,
			"m3" to MutationOutcomeKind.SUPERSEDED
		)
		val repository = FakeEvaluationRepository(outcomeOf = { id -> MutationOutcome(id, outcomes.getValue(id)) })

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2"), draft("m3")))

		assertEquals(
			IntentExecution(
				OutcomeKind.CONFIRMED,
				itemOutcomes = listOf(
					item("m1", OutcomeKind.CONFIRMED),
					item("m2", OutcomeKind.CONFLICT),
					item("m3", OutcomeKind.FAILED, "SUPERSEDED")
				)
			),
			execution
		)
	}

	@Test
	fun add_aBatchThatLandedNothingAnswersItsWorstItemWithItsDetail() = runTest {
		val outcomes = mapOf(
			"m1" to MutationOutcome("m1", MutationOutcomeKind.CONFLICT),
			"m2" to MutationOutcome("m2", MutationOutcomeKind.REJECTED, "bad_grade"),
			"m3" to MutationOutcome("m3", MutationOutcomeKind.CANCELLED)
		)
		val repository = FakeEvaluationRepository(outcomeOf = outcomes::get)

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2"), draft("m3")))

		assertEquals(OutcomeKind.REJECTED, execution.outcome)
		assertEquals("bad_grade", execution.detailCode)
		assertEquals(listOf("m1", "m2", "m3"), execution.itemOutcomes.map { outcome -> outcome.mutationId })
	}

	@Test
	fun add_aMutationStillRunningAfterTheTimeoutIsQueued() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = { null })

		val execution = evaluationsSource(repository).add(addArgs(draft("m1")))

		assertEquals(IntentExecution(OutcomeKind.QUEUED), execution)
		assertTrue(testScheduler.currentTime >= MUTATION_OUTCOME_TIMEOUT_MILLIS)
	}

	@Test
	fun add_aBatchWithAQueuedItemIsQueuedWhenTheOthersConfirm() = runTest {
		val repository = FakeEvaluationRepository(
			outcomeOf = { id -> if (id == "m1") MutationOutcome(id, MutationOutcomeKind.CONFIRMED) else null }
		)

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2")))

		assertEquals(
			IntentExecution(
				OutcomeKind.QUEUED,
				itemOutcomes = listOf(item("m1", OutcomeKind.CONFIRMED), item("m2", OutcomeKind.QUEUED))
			),
			execution
		)
	}

	@Test
	fun add_refusesTheWholeBatchOnAnUnknownTypeWithoutMutating() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2", evaluationType = "NOPE")))

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun add_readsTheScheduleModeByItsWireNameNotByTheNameOfTheEnum() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).add(addArgs(draft("m1", scheduleMode = "DATED")))

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun add_runsOnlyTheSelectedItems() = runTest {
		val repository = FakeEvaluationRepository()

		evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2", selected = false)))

		assertEquals(listOf("m1"), repository.mutations)
	}

	@Test
	fun add_anItemWhoseUseCaseFailsIsFailedAtOnceWithoutWaitingForIt() = runTest {
		val repository = FakeEvaluationRepository(failure = IllegalStateException("the outbox is down"))

		val execution = evaluationsSource(repository).add(addArgs(draft("m1")))

		assertEquals(IntentExecution(OutcomeKind.FAILED, "unhandled"), execution)
		assertEquals(0L, testScheduler.currentTime)
	}

	@Test
	fun edit_mapsTheChangeAndAnswersItsOutcome() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).edit(
			EditEvaluationArgs(
				mutationId = "m1",
				evaluationId = "e1",
				grade = 3.0,
				maxGrade = 5.0,
				dateMillis = null,
				evaluationType = "QUIZ",
				scheduleMode = "continuous"
			),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED), execution)
		assertEquals("e1", repository.updates.single().id)
		assertEquals(3.0, repository.updates.single().grade)
	}

	@Test
	fun edit_thatLeavesTheGradeOutKeepsTheGradeTheEvaluationHas() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).edit(
			EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", dateMillis = 1_800_000_000_000L),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED), execution)
		assertEquals(4.0, repository.updates.single().grade)
		assertEquals(1_800_000_000_000L, repository.updates.single().date)
	}

	@Test
	fun edit_refusesAnEvaluationTheAppDoesNotHoldWithoutMutating() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).edit(
			EditEvaluationArgs(mutationId = "m1", evaluationId = "gone", grade = 3.0),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_evaluation"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun edit_refusesAnUnknownTypeWithoutMutating() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).edit(
			EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", evaluationType = "NOPE"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun remove_answersItsOutcome() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(MutationOutcomeKind.CONFLICT))

		val execution = evaluationsSource(repository).remove(
			RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
		assertEquals("e1", repository.removes.single().id)
	}

	@Test
	fun remove_ofAnEvaluationThePhoneDoesNotHave_isRefusedAtOnce() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).remove(
			RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e9"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_evaluation"), execution)
		assertEquals(emptyList(), repository.removes)
		assertEquals(0L, testScheduler.currentTime)
	}

	@Test
	fun remove_ofAnEvaluationTheServerNoLongerHas_isAConflict() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(MutationOutcomeKind.REJECTED, "NotFound"))

		val execution = evaluationsSource(repository).remove(
			RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT, "NotFound"), execution)
	}

	private suspend fun assertAddAnswer(kind: MutationOutcomeKind, expected: IntentExecution) {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(kind))

		assertEquals(expected, evaluationsSource(repository).add(addArgs(draft("m1"))))
	}

	@Test
	fun edit_atTheRevisionTheIntentWasBuiltOnRuns_andOneThatMovedOnIsAConflictWithNothingChanged() = runTest {
		val repository = FakeEvaluationRepository()
		repository.stored["e1"] = storedEvaluation(id = "e1", grade = 4.0).copy(revision = 7L)
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 3.0)

		val moved = evaluationsSource(repository).edit(args, expectedRevision = 6L)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), moved)
		assertEquals(emptyList(), repository.updates)

		val current = evaluationsSource(repository).edit(args, expectedRevision = 7L)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED), current)
		assertEquals(listOf("m1"), repository.mutations)
	}

	@Test
	fun remove_atTheRevisionTheIntentWasBuiltOnRuns_andOneThatMovedOnIsAConflictWithNothingRemoved() = runTest {
		val repository = FakeEvaluationRepository()
		repository.stored["e1"] = storedEvaluation(id = "e1", grade = 4.0).copy(revision = 7L)
		val args = RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1")

		assertEquals(
			IntentExecution(OutcomeKind.CONFLICT),
			evaluationsSource(repository).remove(args, expectedRevision = 8L)
		)
		assertEquals(emptyList(), repository.removes)

		assertEquals(
			IntentExecution(OutcomeKind.CONFIRMED),
			evaluationsSource(repository).remove(args, expectedRevision = 7L)
		)
		assertEquals("e1", repository.removes.single().id)
	}

	@Test
	fun edit_withoutAnExpectedRevisionOrOfAnEvaluationWhoseRevisionThePhoneDoesNotKnow_isNotChecked() = runTest {
		val repository = FakeEvaluationRepository()
		val args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", grade = 3.0)

		assertEquals(
			IntentExecution(OutcomeKind.CONFIRMED),
			evaluationsSource(repository).edit(args, expectedRevision = 6L)
		)

		repository.stored["e1"] = storedEvaluation(id = "e1", grade = 4.0).copy(revision = 7L)
		assertEquals(
			IntentExecution(OutcomeKind.CONFIRMED),
			evaluationsSource(repository).edit(args, expectedRevision = null)
		)
	}

	@Test
	fun add_aMutationQueuedByTheTimeoutIsFollowedInTheSession_andItsLaterOutcomeReachesTheIntent() = runTest {
		val repository = FakeEvaluationRepository(
			outcomeOf = { id -> if (id == "m1") MutationOutcome(id, MutationOutcomeKind.CONFIRMED) else null }
		)
		val queued = queuedMutationsOf(StandardTestDispatcher(testScheduler), FixedTestClock(nowMillis = 0L))
		val source = evaluationsSource(repository, queued)

		val execution = source.add(addArgs(draft("m1"), draft("m2")))
		assertEquals(OutcomeKind.QUEUED, execution.outcome)

		val delivered = mutableListOf<List<ChatMutationOutcome>>()
		queued.followQueued(listOf("m2"), untilMillis = QUEUED_FOLLOW_LIMIT_MILLIS) { settled -> delivered += settled }
		runCurrent()
		assertEquals(emptyList(), delivered)

		// The outbox settles m2 after the intent answered: the session still listens, and hands its outcome over.
		repository.emitOutcome(MutationOutcome("m2", MutationOutcomeKind.CONFIRMED))
		runCurrent()

		assertEquals(listOf(listOf(item("m2", OutcomeKind.CONFIRMED))), delivered)
	}

	@Test
	fun add_aQueuedMutationThatDoesNotSettleBeforeTheIntentsDeadlineIsGivenUp() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = { null })
		val queued = queuedMutationsOf(StandardTestDispatcher(testScheduler), FixedTestClock(nowMillis = 0L))

		evaluationsSource(repository, queued).add(addArgs(draft("m1")))

		val delivered = mutableListOf<List<ChatMutationOutcome>>()
		queued.followQueued(listOf("m1"), untilMillis = 60_000L) { settled -> delivered += settled }
		advanceTimeBy(61_000L)
		repository.emitOutcome(MutationOutcome("m1", MutationOutcomeKind.CONFIRMED))
		runCurrent()

		assertEquals(emptyList(), delivered)
		assertEquals(0, repository.outcomeSubscribers())
	}

	private fun item(mutationId: String, outcome: OutcomeKind, detailCode: String? = null) =
		ChatMutationOutcome(mutationId = mutationId, outcome = outcome, detailCode = detailCode)

	private fun evaluationsSource(
		repository: FakeEvaluationRepository,
		queued: AssistantQueuedMutationsDataSource = queuedMutationsOf()
	): EvaluationsAssistantDataSource {
		val reporting = RecordingReportingRepository()

		return EvaluationsAssistantDataSource(
			addEvaluationUseCase = AddEvaluationUseCase(
				evaluationRepository = repository,
				identifierRepository = FakeIdentifierRepository(),
				reportingRepository = reporting,
				paramsValidator = AddEvaluationParamsValidator(),
				exceptionHandler = AddEvaluationExceptionHandler()
			),
			updateEvaluationUseCase = UpdateEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reporting,
				exceptionHandler = UpdateEvaluationExceptionHandler()
			),
			removeEvaluationUseCase = RemoveEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reporting,
				exceptionHandler = RemoveEvaluationExceptionHandler()
			),
			getEvaluationUseCase = GetEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reporting
			),
			observeEvaluationMutationOutcomesUseCase = ObserveEvaluationMutationOutcomesUseCase(
				evaluationRepository = repository,
				reportingRepository = reporting
			),
			queuedMutationsDataSource = queued
		)
	}

	private fun addArgs(vararg items: EvaluationDraft) = AddEvaluationsArgs(items = items.toList())

	private fun draft(
		mutationId: String,
		evaluationType: String = "QUIZ",
		scheduleMode: String = "dated",
		selected: Boolean = true
	) = EvaluationDraft(
		mutationId = mutationId,
		reference = "app-reference-$mutationId",
		attemptId = "attempt-1",
		subjectCode = "MAT101",
		termId = "term-1",
		evaluationType = evaluationType,
		scheduleMode = scheduleMode,
		dateMillis = 1_700_000_000_000L,
		grade = 4.5,
		maxGrade = 5.0,
		selected = selected
	)
}
