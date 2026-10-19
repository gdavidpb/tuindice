package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordRevisionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordMutationOutcomesUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecordAssistantProjectionDataSourceTest {
	@Test
	fun save_projectsTheGradeAndConfirms() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		val execution = projectionSource(repository).save(
			projection(grade = 4, outcome = null),
			expectedRevision = 4L
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED, newRevision = 5L), execution)

		val call = repository.projections.single()
		assertEquals("a1", call.attemptId)
		assertEquals(AttemptScore.numeric(4), call.score)
		assertNull(call.outcome)
		assertEquals("m1", call.mutationId)
	}

	@Test
	fun save_readsTheOutcomeBySerialNameAndProjectsItWithoutAGrade() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		projectionSource(repository).save(projection(grade = null, outcome = "approved"), expectedRevision = null)

		val call = repository.projections.single()
		assertNull(call.score)
		assertEquals(AttemptOutcome.APPROVED, call.outcome)
	}

	@Test
	fun save_refusesAnOutcomeGivenByItsEnumNameWithoutMutating() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		val execution = projectionSource(repository).save(
			projection(grade = null, outcome = "APPROVED"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.projections)
	}

	@Test
	fun save_refusesAProjectionThatProjectsNothingWithoutMutating() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		val execution = projectionSource(repository).save(
			projection(grade = null, outcome = null, clear = false),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun save_clearSendsNeitherTheGradeNorTheOutcomeEvenWhenTheArgsCarryThem() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		val execution = projectionSource(repository).save(
			projection(grade = 4, outcome = "approved", clear = true),
			expectedRevision = null
		)

		assertEquals(OutcomeKind.CONFIRMED, execution.outcome)

		val call = repository.projections.single()
		assertNull(call.score)
		assertNull(call.outcome)
	}

	@Test
	fun save_answersConflictWithoutMutatingWhenTheRecordMovedOn() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		val execution = projectionSource(repository).save(projection(grade = 4, outcome = null), expectedRevision = 3L)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun save_subscribesToTheOutcomesBeforeItMutates() = runTest {
		val repository = FakeRecordRepository(revision = 4L)

		projectionSource(repository).save(projection(grade = 4, outcome = null), expectedRevision = null)

		assertEquals(listOf(true), repository.subscribedAtMutation)
	}

	private fun projectionSource(repository: FakeRecordRepository): RecordAssistantProjectionDataSource {
		val reporting = RecordingReportingRepository()

		return RecordAssistantProjectionDataSource(
			upsertAttemptSelectionUseCase = UpsertAttemptSelectionUseCase(
				academicRecordRepository = repository,
				reportingRepository = reporting,
				exceptionHandler = RecordExceptionHandler()
			),
			recordIntentRunner = RecordIntentRunnerDataSource(
				getRecordRevisionUseCase = GetRecordRevisionUseCase(
					academicRecordRepository = repository,
					reportingRepository = reporting
				),
				observeRecordMutationOutcomesUseCase = ObserveRecordMutationOutcomesUseCase(
					academicRecordRepository = repository,
					reportingRepository = reporting
				),
				queuedMutationsDataSource = queuedMutationsOf()
			)
		)
	}

	private fun projection(
		grade: Int?,
		outcome: String?,
		clear: Boolean = false
	) = ProjectionArgs(
		mutationId = "m1",
		attemptId = "a1",
		grade = grade,
		outcome = outcome,
		clear = clear
	)
}
