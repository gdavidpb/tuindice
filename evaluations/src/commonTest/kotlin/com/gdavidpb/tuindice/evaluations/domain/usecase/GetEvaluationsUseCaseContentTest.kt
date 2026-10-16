@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GetEvaluationsUseCaseContentTest {
	@Test
	fun getEvaluationsUseCase_whenSnapshotIsNotSynced_emitsContentFlaggedAsNotSynced() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(listOf(DEFAULT_PENDING_EVALUATION)),
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				hasSyncedEvaluationsFlow = flowOf(false)
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertFalse(content.hasSyncedEvaluations)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_whenContentEmitted_exposesAttemptsAndCurrentTermAsDisplayContext() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(listOf(DEFAULT_PENDING_EVALUATION)),
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				availableSubjects = listOf(
					DEFAULT_EVALUATION_SUBJECT,
					SECOND_EVALUATION_SUBJECT
				),
				currentTerm = DEFAULT_EVALUATION_TERM
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				content.displayContext.attempts
			)
			assertEquals(DEFAULT_EVALUATION_TERM, content.displayContext.currentTerm)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_whenTheTermGainsItsFirstDayOfClasses_emitsContentAgainWithTheNewTerm() = runTest {
		val repository = RecordingEvaluationRepository(
			evaluationsFlow = flowOf(listOf(DEFAULT_PENDING_EVALUATION)),
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			currentTerm = DEFAULT_EVALUATION_TERM
		)
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = repository,
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)
		val withClassesStart = DEFAULT_EVALUATION_TERM.copy(classesStartDate = LocalDate(2026, 3, 30))

		useCase.execute(Unit).test {
			val first = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertNull(first.displayContext.currentTerm?.classesStartDate)

			// A sync stores the date while the tab is open: the content comes again with it.
			repository.changeCurrentTerm(withClassesStart)

			val second = assertIs<GetEvaluations.Content>(
				assertIs<UseCaseState.Data<GetEvaluations>>(awaitItem()).value
			)
			assertEquals(withClassesStart, second.displayContext.currentTerm)
			assertEquals(first.displayContext.attempts, second.displayContext.attempts)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_whenTheTermChangesWithTheTabOpen_listAndAttemptsAreOfTheNewTerm() = runTest {
		val repository = repositoryOf(
			evaluations = listOf(DEFAULT_PENDING_EVALUATION, NEXT_TERM_EVALUATION),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			term = DEFAULT_EVALUATION_TERM
		)

		contentUseCase(repository).execute(Unit).test {
			val first = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(listOf(DEFAULT_EVALUATION_SUBJECT), first.displayContext.attempts)
			assertEquals(DEFAULT_EVALUATION_TERM, first.displayContext.currentTerm)
			assertEquals(listOf(DEFAULT_PENDING_EVALUATION), first.listed())

			// A sync moves the record to the next term: the term and its attempts change together.
			repository.changeTermAndAttempts(NEXT_TERM, listOf(NEXT_TERM_SUBJECT))

			val last = awaitContentWhere { content ->
				content.displayContext.currentTerm == NEXT_TERM &&
					content.displayContext.attempts == listOf(NEXT_TERM_SUBJECT)
			}
			assertEquals(listOf(NEXT_TERM_EVALUATION), last.listed())
			cancelAndIgnoreRemainingEvents()
		}
	}

	// The term and its attempts are one observed value: when a write changes both, the tab is told
	// the new pair once, never the attempts of one term under the descriptor of the other.
	@Test
	fun getEvaluationsUseCase_whenTheTermAndItsAttemptsChangeTogether_neverEmitsAMixedPair() = runTest {
		val repository = repositoryOf(
			evaluations = listOf(DEFAULT_PENDING_EVALUATION, NEXT_TERM_EVALUATION),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			term = DEFAULT_EVALUATION_TERM
		)
		val before = DEFAULT_EVALUATION_TERM to listOf(DEFAULT_EVALUATION_SUBJECT)
		val after = NEXT_TERM to listOf(NEXT_TERM_SUBJECT)
		val pairs = mutableListOf<Pair<Any?, Any?>>()

		contentUseCase(repository).execute(Unit).test {
			pairs += assertIs<GetEvaluations.Content>(awaitLoadingThenData(this)).displayContext.toPair()

			repository.changeTermAndAttempts(NEXT_TERM, listOf(NEXT_TERM_SUBJECT))

			pairs += awaitContentWhere { content -> content.displayContext.currentTerm == NEXT_TERM }
				.displayContext.toPair()
			// Nothing else follows from that one write.
			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(listOf<Pair<Any?, Any?>>(before, after), pairs)
	}

	@Test
	fun getEvaluationsUseCase_whenAnAttemptDisappearsWithinTheSameTerm_itsEvaluationsLeaveTheList() = runTest {
		val repository = repositoryOf(
			evaluations = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
			term = DEFAULT_EVALUATION_TERM
		)

		contentUseCase(repository).execute(Unit).test {
			val first = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(
				setOf(DEFAULT_PENDING_EVALUATION.id, DEFAULT_COMPLETED_EVALUATION.id),
				first.listed().map { evaluation -> evaluation.id }.toSet()
			)

			// A withdrawal: the term stays, one of its attempts goes away.
			repository.changeAvailableAttempts(listOf(DEFAULT_EVALUATION_SUBJECT))

			val second = assertIs<GetEvaluations.Content>(
				assertIs<UseCaseState.Data<GetEvaluations>>(awaitItem()).value
			)
			assertEquals(listOf(DEFAULT_EVALUATION_SUBJECT), second.displayContext.attempts)
			assertEquals(DEFAULT_EVALUATION_TERM, second.displayContext.currentTerm)
			assertEquals(listOf(DEFAULT_PENDING_EVALUATION), second.listed())
			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_followsTheStepBetweenNoAttemptsAndContentInBothDirections() = runTest {
		val repository = repositoryOf(
			evaluations = listOf(DEFAULT_PENDING_EVALUATION),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			term = DEFAULT_EVALUATION_TERM
		)

		contentUseCase(repository).execute(Unit).test {
			assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))

			repository.changeAvailableAttempts(emptyList())
			assertIs<GetEvaluations.NoAttempts>(
				assertIs<UseCaseState.Data<GetEvaluations>>(awaitItem()).value
			)

			repository.changeAvailableAttempts(listOf(DEFAULT_EVALUATION_SUBJECT))
			val back = assertIs<GetEvaluations.Content>(
				assertIs<UseCaseState.Data<GetEvaluations>>(awaitItem()).value
			)
			assertEquals(listOf(DEFAULT_EVALUATION_SUBJECT), back.displayContext.attempts)
			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun GetEvaluations.Content.listed(): List<Evaluation> {
		return evaluations.listedUnder(displayContext.attempts)
	}

	private suspend fun ReceiveTurbine<UseCaseState<GetEvaluations, EvaluationsUseCaseError>>.awaitContentWhere(
		predicate: (GetEvaluations.Content) -> Boolean
	): GetEvaluations.Content {
		repeat(MAX_INTERMEDIATE_EMISSIONS) {
			val state = awaitItem()
			val content = (state as? UseCaseState.Data<*>)?.value as? GetEvaluations.Content

			if (content != null && predicate(content)) return content
		}

		throw AssertionError("The expected content never arrived")
	}

	private fun repositoryOf(
		evaluations: List<Evaluation>,
		attempts: List<EditableAttemptDescriptor>,
		term: EvaluationTermDescriptor?
	) = RecordingEvaluationRepository(
		initialEvaluations = evaluations,
		availableSubjects = attempts,
		currentTerm = term
	)

	private fun contentUseCase(repository: RecordingEvaluationRepository) = GetEvaluationsUseCase(
		evaluationRepository = repository,
		recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
		syncStatusRepository = RecordingSyncStatusRepository(),
		evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
		reportingRepository = RecordingReportingRepository(),
		clock = Clock.System
	)

	@Test
	fun getEvaluationsUseCase_whenDatesShareTimeBucket_breaksTiesByEvaluationState() = runTest {
		val currentTime = currentTimeMillis()
		val pastOverdue = DEFAULT_PENDING_EVALUATION.copy(
			id = "evaluation-overdue",
			date = currentTime - ONE_DAY_MILLIS,
			state = EvaluationState.OVERDUE
		)
		val pastCompleted = DEFAULT_COMPLETED_EVALUATION.copy(
			date = currentTime - 2 * ONE_DAY_MILLIS
		)
		val continuousWithoutDate = DEFAULT_PENDING_EVALUATION.copy(
			id = "evaluation-continuous",
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = null,
			state = EvaluationState.CONTINUOUS
		)
		val futurePending = DEFAULT_PENDING_EVALUATION.copy(
			date = currentTime + ONE_DAY_MILLIS
		)
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(
					listOf(
						futurePending,
						continuousWithoutDate,
						pastCompleted,
						pastOverdue
					)
				),
				initialEvaluations = listOf(
					futurePending,
					continuousWithoutDate,
					pastCompleted,
					pastOverdue
				)
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(
				listOf(pastOverdue, pastCompleted, continuousWithoutDate, futurePending),
				content.evaluations
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_whenAttemptsLookupFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("attempts unavailable")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				availableAttemptsThrowable = throwable
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = reportingRepository,
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		// kotlinx-coroutines stack-trace recovery copies exceptions across dispatch
		// boundaries on the JVM, so instance identity is platform-dependent.
		val logged = reportingRepository.exceptions.single()
		assertEquals(throwable::class, logged::class)
		assertEquals(throwable.message, logged.message)
	}
}

private const val ONE_DAY_MILLIS = 86_400_000L

private fun EvaluationDisplayContext.toPair(): Pair<Any?, Any?> = currentTerm to attempts

private const val MAX_INTERMEDIATE_EMISSIONS = 4
