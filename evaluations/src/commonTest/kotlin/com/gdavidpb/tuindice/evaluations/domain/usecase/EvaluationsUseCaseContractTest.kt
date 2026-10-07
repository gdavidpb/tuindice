@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.testing.*
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EvaluationsUseCaseContractTest {
	@Test
	fun getEvaluationsUseCase_emitsSortedEvaluations() = runTest {
		val currentTime = currentTimeMillis()
		val futureEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			date = currentTime + ONE_DAY_MILLIS
		)
		val pastEvaluation = DEFAULT_COMPLETED_EVALUATION.copy(
			date = currentTime - ONE_DAY_MILLIS
		)
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(
					listOf(
						futureEvaluation,
						pastEvaluation
					)
				),
				initialEvaluations = listOf(
					futureEvaluation,
					pastEvaluation
				)
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			val result = awaitLoadingThenData(this) as GetEvaluations.Content
			assertEquals(
				listOf(pastEvaluation, futureEvaluation),
				result.evaluations
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_returnsNoAttemptsState_whenFeatureHasNoAttempts() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = reportingRepository,
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.NoCurrentTerm),
				awaitLoadingThenData(this)
			)
			assertTrue(reportingRepository.exceptions.isEmpty())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_returnsEnrollmentUnavailableNoAttemptsState_whenEnrollmentSyncFailed() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = SyncReport.partialEnrollmentUnavailable()
			),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.EnrollmentUnavailable),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_waitsForRecordData_beforeCheckingAttempts() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
				states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = false))
			),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			assertEquals(GetEvaluations.WaitingForRecordData, awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationsUseCase_reportsRecordUnavailable_whenPrerequisiteFailed() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
				states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = true))
			),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		useCase.execute(Unit).test {
			assertEquals(GetEvaluations.RecordDataUnavailable(), awaitLoadingThenData(this))
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationAndAvailableAttemptsUseCase_emitsEvaluationAndAttempts() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
		val useCase = GetEvaluationAndAvailableAttemptsUseCase(
			evaluationRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(GetEvaluationParams(DEFAULT_PENDING_EVALUATION.id)).test {
			val result = awaitLoadingThenData(this)
			assertEquals(DEFAULT_PENDING_EVALUATION, result.evaluation)
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				result.availableAttempts
			)
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationsUseCase_reportsNewStudent_whenPrerequisiteFailedBecauseOfNoRecord() = runTest {
		val useCase = noAttemptsUseCase(
			syncStatusRepository = RecordingSyncStatusRepository(
				initialStatus = SyncStatus.NewStudentNoRecord
			),
			prerequisite = RecordDataPrerequisiteState(isReady = false, hasFailed = true)
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.RecordDataUnavailable(isNewStudentNoRecord = true),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_withNoCurrentTermAndSituation_reportsNoCurrentTerm() = runTest {
		val useCase = noAttemptsUseCase(
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = enrollmentReport(
					status = SyncSourceStatus.Success,
					situation = EnrollmentSituation(code = "15")
				)
			)
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.NoCurrentTerm),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	// The final annulment arrives as "not enrolled" together with its situation. The situation
	// wins: the record and the summary explain it, and here it is only a term that is not there.
	@Test
	fun getEvaluationsUseCase_withNotEnrolledAndSituation_reportsNoCurrentTermInsteadOfNotEnrolled() = runTest {
		val useCase = noAttemptsUseCase(
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = enrollmentReport(
					status = SyncSourceStatus.NotEnrolled,
					situation = EnrollmentSituation(code = "15")
				)
			)
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.NoCurrentTerm),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_withNoCurrentTermAndNotEnrolled_reportsNotEnrolled() = runTest {
		val useCase = noAttemptsUseCase(
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = enrollmentReport(status = SyncSourceStatus.NotEnrolled)
			)
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.NotEnrolled),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_withCurrentTermAndSituation_observesTheSameContentAsWithoutIt() = runTest {
		fun contentUseCase(situation: EnrollmentSituation?) = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(listOf(DEFAULT_PENDING_EVALUATION)),
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION)
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = enrollmentReport(
					status = SyncSourceStatus.Success,
					situation = situation
				)
			),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)

		var contentWithoutSituation: GetEvaluations? = null

		contentUseCase(situation = null).execute(Unit).test {
			contentWithoutSituation = awaitLoadingThenData(this)
			cancelAndIgnoreRemainingEvents()
		}

		// The provisional annulment keeps the term: the list is the one it would be without it.
		contentUseCase(situation = EnrollmentSituation(code = "01")).execute(Unit).test {
			val content = awaitLoadingThenData(this) as GetEvaluations.Content

			assertEquals(listOf(DEFAULT_PENDING_EVALUATION), content.evaluations)
			assertEquals(contentWithoutSituation, content)
			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun noAttemptsUseCase(
		syncStatusRepository: RecordingSyncStatusRepository,
		prerequisite: RecordDataPrerequisiteState = RecordDataPrerequisiteState(
			isReady = true,
			hasFailed = false
		)
	): GetEvaluationsUseCase {
		return GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
				states = flowOf(prerequisite)
			),
			syncStatusRepository = syncStatusRepository,
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		)
	}

	private fun enrollmentReport(
		status: SyncSourceStatus,
		situation: EnrollmentSituation? = null
	): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(status = status, situation = situation)
			)
		)
	}
}

private const val ONE_DAY_MILLIS = 86_400_000L
