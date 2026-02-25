package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsRepository
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.sign

class GetEvaluationsUseCase(
	private val quarterRepository: QuarterRepository,
	private val evaluationRepository: EvaluationRepository,
	private val filterLabelsProvider: EvaluationFilterLabelsRepository,
	override val exceptionHandler: GetEvaluationsExceptionHandler
) : FlowUseCase<Flow<List<EvaluationFilter>>, GetEvaluations, EvaluationsUseCaseError>() {

	private val evaluationComparator =
		Comparator<Evaluation> { a, b ->
			val currentTime = currentTimeMillis()

			val aDate = a.date ?: 0
			val bDate = b.date ?: 0

			(aDate - currentTime).sign.compareTo((bDate - currentTime).sign)
		}
			.then(compareBy(Evaluation::state))

	override suspend fun executeOnBackground(params: Flow<List<EvaluationFilter>>): Flow<GetEvaluations> {
		val availableSubjects = evaluationRepository
			.getAvailableSubjects()

		if (availableSubjects.isEmpty()) {
			quarterRepository.getQuarters()

			val refreshedAvailableSubjects = evaluationRepository
				.getAvailableSubjects()

			if (refreshedAvailableSubjects.isEmpty())
				throw NoSubjectsException()
		}

			return evaluationRepository.getEvaluationsFlow()
				.combine(params) { evaluations, activeFilters ->
					val availableFilters = evaluations.computeAvailableFilters(filterLabelsProvider)
					val sortedEvaluations = evaluations.sortedWith(evaluationComparator)

				val filteredEvaluations = if (activeFilters.isEmpty())
					sortedEvaluations
				else
					activeFilters
						.groupBy { filter -> filter::class }
						.values
						.fold(initial = sortedEvaluations) { acc, filters ->
							acc.filter { evaluation ->
								filters.any { filter -> filter.match(evaluation) }
							}
						}

				GetEvaluations(
					originalEvaluations = sortedEvaluations,
					filteredEvaluations = filteredEvaluations,
					availableFilters = availableFilters,
					activeFilters = activeFilters
				)
			}
	}
}
