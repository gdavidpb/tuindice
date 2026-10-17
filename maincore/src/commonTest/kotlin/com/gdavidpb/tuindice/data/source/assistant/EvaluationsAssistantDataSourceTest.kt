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
import com.gdavidpb.tuindice.evaluations.domain.usecase.ObserveEvaluationMutationOutcomesUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EvaluationsAssistantDataSourceTest {
	@Test
	fun add_subscribesToTheOutcomesBeforeAnyMutationRuns() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2")))

		assertEquals(listOf("m1", "m2"), repository.mutations)
		assertEquals(listOf(true, true), repository.subscribedAtMutation)
		assertEquals(IntentExecution(OutcomeKind.CONFIRMED), execution)
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
	fun add_aBatchAnswersWithItsWorstItem() = runTest {
		val outcomes = mapOf(
			"m1" to MutationOutcomeKind.CONFIRMED,
			"m2" to MutationOutcomeKind.CONFLICT,
			"m3" to MutationOutcomeKind.SUPERSEDED
		)
		val repository = FakeEvaluationRepository(outcomeOf = { id -> MutationOutcome(id, outcomes.getValue(id)) })

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2"), draft("m3")))

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
	}

	@Test
	fun add_aBatchWithARejectionAnswersTheRejectionWithItsDetail() = runTest {
		val outcomes = mapOf(
			"m1" to MutationOutcome("m1", MutationOutcomeKind.CONFIRMED),
			"m2" to MutationOutcome("m2", MutationOutcomeKind.REJECTED, "bad_grade"),
			"m3" to MutationOutcome("m3", MutationOutcomeKind.CONFLICT)
		)
		val repository = FakeEvaluationRepository(outcomeOf = outcomes::get)

		val execution = evaluationsSource(repository).add(addArgs(draft("m1"), draft("m2"), draft("m3")))

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "bad_grade"), execution)
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

		assertEquals(IntentExecution(OutcomeKind.QUEUED), execution)
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
			)
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED), execution)
		assertEquals("e1", repository.updates.single().id)
		assertEquals(3.0, repository.updates.single().grade)
	}

	@Test
	fun edit_refusesAnUnknownTypeWithoutMutating() = runTest {
		val repository = FakeEvaluationRepository()

		val execution = evaluationsSource(repository).edit(
			EditEvaluationArgs(mutationId = "m1", evaluationId = "e1", evaluationType = "NOPE")
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun remove_answersItsOutcome() = runTest {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(MutationOutcomeKind.CONFLICT))

		val execution = evaluationsSource(repository).remove(RemoveEvaluationArgs(mutationId = "m1", evaluationId = "e1"))

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
		assertEquals("e1", repository.removes.single().id)
	}

	private suspend fun assertAddAnswer(kind: MutationOutcomeKind, expected: IntentExecution) {
		val repository = FakeEvaluationRepository(outcomeOf = settledAs(kind))

		assertEquals(expected, evaluationsSource(repository).add(addArgs(draft("m1"))))
	}

	private fun evaluationsSource(repository: FakeEvaluationRepository): EvaluationsAssistantDataSource {
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
			observeEvaluationMutationOutcomesUseCase = ObserveEvaluationMutationOutcomesUseCase(
				evaluationRepository = repository,
				reportingRepository = reporting
			)
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
