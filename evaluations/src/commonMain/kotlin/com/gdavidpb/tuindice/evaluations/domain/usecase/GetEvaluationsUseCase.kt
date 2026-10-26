@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.sign
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class GetEvaluationsUseCase(
	private val evaluationRepository: EvaluationRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val evaluationsSelectionRepository: EvaluationsSelectionRepository,
	override val reportingRepository: ReportingRepository,
	private val clock: Clock
) : FlowUseCase<Unit, GetEvaluations, EvaluationsUseCaseError>() {

	private val evaluationComparator =
		Comparator<Evaluation> { a, b ->
			val currentTime = clock.now().toEpochMilliseconds()

			val aDate = a.date ?: 0
			val bDate = b.date ?: 0

			(aDate - currentTime).sign.compareTo((bDate - currentTime).sign)
		}
			.then(compareBy(Evaluation::state))

	override suspend fun executeOnBackground(params: Unit): Flow<GetEvaluations> {
		return recordDataPrerequisiteRepository
			.observeRecordDataPrerequisiteFlow()
			.flatMapLatest { prerequisite ->
				when {
					prerequisite.hasFailed -> syncStatusRepository.observeSyncStatus()
						.map { syncStatus ->
							GetEvaluations.RecordDataUnavailable(
								isNewStudentNoRecord = syncStatus == SyncStatus.NewStudentNoRecord
							)
						}
					!prerequisite.isReady -> flowOf(GetEvaluations.WaitingForRecordData)
					else -> observeReadyEvaluations()
				}
			}
	}

	// The term and its attempts are observed, not read once, and as ONE value: the list and the display
	// context come from the same read, so a term change or a withdrawal reaches both together (never
	// the attempts of one term under the descriptor of another), and the step between "no attempts"
	// and content is followed in both directions.
	private fun observeReadyEvaluations(): Flow<GetEvaluations> {
		return evaluationRepository
			.observeDisplayContextFlow()
			.flatMapLatest { displayContext ->
				if (displayContext.attempts.isEmpty()) {
					observeNoAttempts()
				} else {
					observeContent(displayContext)
				}
			}
	}

	private fun observeNoAttempts(): Flow<GetEvaluations> {
		return syncStatusRepository.observeSyncReport()
			.map { syncReport ->
				val enrollment = syncReport.sources.enrollment

				GetEvaluations.NoAttempts(
					reason = when {
						// A final annulment drops the term, and the university reports it together
						// with "not enrolled". It has to win: the record and the summary are the
						// ones that explain it, and here it reads as having no current term.
						enrollment.situation != null ->
							EvaluationsNoAttemptsReason.NoCurrentTerm

						enrollment.status == SyncSourceStatus.NotEnrolled ->
							EvaluationsNoAttemptsReason.NotEnrolled

						enrollment.status == SyncSourceStatus.Unavailable ->
							EvaluationsNoAttemptsReason.EnrollmentUnavailable

						else -> EvaluationsNoAttemptsReason.NoCurrentTerm
					}
				)
			}
	}

	// A sync can bring the first day of classes while the tab is open, and the weeks hang from it: the
	// term is part of the display context, so it restarts this observation like the attempts do.
	private suspend fun observeContent(
		displayContext: EvaluationDisplayContext
	): Flow<GetEvaluations> {
		// The selected week is read on each snapshot instead of observed: saving a week must not re-emit the
		// content, and a snapshot after Empty still carries the last week the user picked.
		return evaluationRepository.observeEvaluationsSnapshotFlow().map { snapshot ->
			GetEvaluations.Content(
				evaluations = snapshot.value.sortedWith(evaluationComparator),
				hasSyncedEvaluations = snapshot.hasSynced,
				displayContext = displayContext,
				selectedWeekKey = evaluationsSelectionRepository.observeSelectedWeekKey().first()
			)
		}
	}
}
