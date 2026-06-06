package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlin.math.sign

@OptIn(ExperimentalCoroutinesApi::class)
class GetEvaluationsUseCase(
	private val evaluationRepository: EvaluationRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, GetEvaluations, EvaluationsUseCaseError>(reportingRepository = reportingRepository) {

	private val evaluationComparator =
		Comparator<Evaluation> { a, b ->
			val currentTime = currentTimeMillis()

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
					prerequisite.hasFailed -> flowOf(GetEvaluations.RecordDataUnavailable)
					!prerequisite.isReady -> flowOf(GetEvaluations.WaitingForRecordData)
					else -> observeReadyEvaluations()
				}
			}
	}

	private suspend fun observeReadyEvaluations(): Flow<GetEvaluations> {
		val availableAttempts = evaluationRepository.getAvailableAttempts()
		if (availableAttempts.isEmpty()) return flowOf(GetEvaluations.NoAttempts)
		val displayContext = EvaluationDisplayContext(
			attempts = availableAttempts,
			currentTerm = evaluationRepository.getCurrentTerm()
		)

		return combine(
			evaluationRepository.observeEvaluationsFlow(),
			evaluationRepository.observeHasSyncedEvaluationsFlow()
		) { evaluations, hasSyncedEvaluations ->
			GetEvaluations.Content(
				evaluations = evaluations.sortedWith(evaluationComparator),
				hasSyncedEvaluations = hasSyncedEvaluations,
				displayContext = displayContext
			)
		}
	}
}
