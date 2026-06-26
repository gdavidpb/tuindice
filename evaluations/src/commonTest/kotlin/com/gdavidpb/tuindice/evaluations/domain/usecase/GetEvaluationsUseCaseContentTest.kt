package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

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
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertFalse(content.hasSyncedEvaluations)
			awaitComplete()
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
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				content.displayContext.attempts
			)
			assertEquals(DEFAULT_EVALUATION_TERM, content.displayContext.currentTerm)
			awaitComplete()
		}
	}

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
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val content = assertIs<GetEvaluations.Content>(awaitLoadingThenData(this))
			assertEquals(
				listOf(pastOverdue, pastCompleted, continuousWithoutDate, futurePending),
				content.evaluations
			)
			awaitComplete()
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
			reportingRepository = reportingRepository
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
