package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordRevisionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordMutationOutcomesUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class RecordAssistantPlansDataSourceTest {
	@Test
	fun create_takesTheNameAndTheCreditsOfEachSubjectFromThePensumAndConfirms() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)

		val execution = plansSource(repository).create(createArgs(subjectCodes = listOf("mat101")), expectedRevision = 4L)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED, newRevision = 5L), execution)

		val command = repository.addedTerms.single()
		assertEquals("m1", command.mutationId)
		assertEquals(2099, command.periodYear)
		assertEquals(AcademicTermPeriod.APR_JUL, command.periodCode)
		assertEquals(
			listOf(Triple("MAT101", "Cálculo I", 5)),
			command.attempts.map { attempt -> Triple(attempt.subjectCode, attempt.subjectName, attempt.credits) }
		)
	}

	@Test
	fun create_refusesAnUnknownSubjectWithoutMutating() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)

		val execution = plansSource(repository).create(
			createArgs(subjectCodes = listOf(CALCULUS_CODE, "XYZ999")),
			expectedRevision = 4L
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_subject"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun create_refusesAnUnknownPeriodWithoutMutating() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)

		val execution = plansSource(repository).create(createArgs(periodCode = "NOPE"), expectedRevision = 4L)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "invalid_args"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun create_answersConflictWithoutMutatingWhenTheRecordMovedOn() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)

		val execution = plansSource(repository).create(createArgs(), expectedRevision = 3L)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun create_subscribesToTheOutcomesBeforeItMutates() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)

		val execution = plansSource(repository).create(createArgs(), expectedRevision = null)

		assertEquals(listOf(true), repository.subscribedAtMutation)
		assertEquals(OutcomeKind.CONFIRMED, execution.outcome)
	}

	@Test
	fun create_aMutationStillRunningAfterTheTimeoutIsQueued() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L, outcomeOf = { null })

		val execution = plansSource(repository).create(createArgs(), expectedRevision = null)

		assertEquals(IntentExecution(OutcomeKind.QUEUED), execution)
		assertTrue(testScheduler.currentTime >= MUTATION_OUTCOME_TIMEOUT_MILLIS)
	}

	@Test
	fun create_aPensumThatFailsToReadIsFailedWithTheNameOfTheException() = runTest {
		val repository = FakeRecordRepository(record = recordWith(), revision = 4L)
		val unreadable = flow<PensumObservation> {
			throw IllegalStateException("the pensum is corrupt")
		}

		val execution = plansSource(repository, pensum = FakePensumRepository(unreadable))
			.create(createArgs(), expectedRevision = null)

		assertEquals(IntentExecution(OutcomeKind.FAILED, "IllegalStateException"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun edit_keepsTheAttemptsTheTermHoldsAndTakesTheOtherSubjectsFromThePensum() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).edit(
			EditPlanArgs(mutationId = "m1", termKey = TERM_KEY, subjectCodes = listOf(CALCULUS_CODE, PHYSICS_CODE)),
			expectedRevision = 4L
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED, newRevision = 5L), execution)

		val command = repository.updatedTerms.single()
		// The term is found by its key and written under its id, which the record holds apart from the key.
		assertEquals(TERM_ID, command.targetTermId)
		assertEquals(TERM_KEY, command.targetTermKey)
		assertEquals(
			listOf(
				Triple("MAT101", "Cálculo I", 5),
				Triple("FIS200", "Física II", 4)
			),
			command.attempts.map { attempt -> Triple(attempt.subjectCode, attempt.subjectName, attempt.credits) }
		)
		assertEquals("a1", command.attempts.first().attemptId)
	}

	@Test
	fun edit_refusesAnUnknownTermWithoutMutating() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).edit(
			EditPlanArgs(mutationId = "m1", termKey = "2099-JUL_AUG", subjectCodes = listOf(CALCULUS_CODE)),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_term"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun edit_refusesAnUnknownSubjectWithoutMutating() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).edit(
			EditPlanArgs(mutationId = "m1", termKey = TERM_KEY, subjectCodes = listOf("XYZ999")),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_subject"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun edit_answersConflictWithoutMutatingWhenTheRecordMovedOn() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).edit(
			EditPlanArgs(mutationId = "m1", termKey = TERM_KEY, subjectCodes = listOf(CALCULUS_CODE)),
			expectedRevision = 2L
		)

		assertEquals(IntentExecution(OutcomeKind.CONFLICT), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun remove_deletesTheTermTheRecordHoldsForTheKeyByItsId() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).remove(
			RemovePlanArgs(mutationId = "m1", termKey = TERM_KEY),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.CONFIRMED, newRevision = 5L), execution)
		assertEquals(listOf(TERM_ID), repository.deletedTermIds)
	}

	@Test
	fun remove_refusesAnUnknownTermWithoutMutating() = runTest {
		val repository = FakeRecordRepository(record = recordWith(term()), revision = 4L)

		val execution = plansSource(repository).remove(
			RemovePlanArgs(mutationId = "m1", termKey = "2099-JUL_AUG"),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "unknown_term"), execution)
		assertEquals(emptyList(), repository.mutations)
	}

	@Test
	fun remove_aRejectionOfTheRecordIsAnsweredWithItsDetail() = runTest {
		val repository = FakeRecordRepository(
			record = recordWith(term()),
			revision = 4L,
			outcomeOf = { id -> MutationOutcome(id, MutationOutcomeKind.REJECTED, "term_in_use") }
		)

		val execution = plansSource(repository).remove(
			RemovePlanArgs(mutationId = "m1", termKey = TERM_KEY),
			expectedRevision = null
		)

		assertEquals(IntentExecution(OutcomeKind.REJECTED, "term_in_use"), execution)
	}

	private fun plansSource(
		repository: FakeRecordRepository,
		pensum: PensumRepository = FakePensumRepository(flowOf(pensumWith(CALCULUS, PHYSICS)))
	): RecordAssistantPlansDataSource {
		val reporting = RecordingReportingRepository()
		val clock = Clock.System

		return RecordAssistantPlansDataSource(
			createSyntheticTermUseCase = CreateSyntheticTermUseCase(
				repository = repository,
				reportingRepository = reporting,
				exceptionHandler = RecordExceptionHandler(),
				clock = clock
			),
			updateSyntheticTermUseCase = UpdateSyntheticTermUseCase(
				repository = repository,
				reportingRepository = reporting,
				exceptionHandler = RecordExceptionHandler(),
				clock = clock
			),
			deleteSyntheticTermUseCase = DeleteSyntheticTermUseCase(
				academicRecordRepository = repository,
				reportingRepository = reporting,
				exceptionHandler = RecordExceptionHandler()
			),
			recordIntentRunner = RecordIntentRunner(
				getRecordRevisionUseCase = GetRecordRevisionUseCase(
					academicRecordRepository = repository,
					reportingRepository = reporting
				),
				observeRecordMutationOutcomesUseCase = ObserveRecordMutationOutcomesUseCase(
					academicRecordRepository = repository,
					reportingRepository = reporting
				)
			),
			academicRecordRepository = repository,
			pensumSubjectCatalog = PensumSubjectCatalog(pensum)
		)
	}

	private fun createArgs(
		periodCode: String = AcademicTermPeriod.APR_JUL.name,
		subjectCodes: List<String> = listOf(CALCULUS_CODE)
	) = CreatePlanArgs(
		mutationId = "m1",
		periodYear = 2099,
		periodCode = periodCode,
		subjectCodes = subjectCodes
	)

	// The synthetic term the record holds: an id that is not its key, and one subject with its attempt.
	private fun term() = syntheticTerm(
		id = TERM_ID,
		attempts = listOf(attempt(id = "a1", subjectCode = CALCULUS_CODE, subjectName = "Cálculo I", credits = 5))
	)

	private companion object {
		val CALCULUS = Triple(CALCULUS_CODE, "Cálculo I", 5)
		val PHYSICS = Triple(PHYSICS_CODE, "Física II", 4)

		const val CALCULUS_CODE = "MAT101"
		const val PHYSICS_CODE = "FIS200"
		const val TERM_ID = "term-2099-apr-jul"
		const val TERM_KEY = "2099-APR_JUL"
	}
}
