package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.sign

class GetEvaluationsUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	private val resourceResolver: ResourceResolver
) : FlowUseCase<Flow<List<EvaluationFilter>>, GetEvaluations, EvaluationsError>() {

	private val evaluationComparator =
		Comparator<Evaluation> { a, b ->
			val currentTime = System.currentTimeMillis()

			(a.date - currentTime).sign + (b.date - currentTime).sign
		}
			.then(compareBy(Evaluation::state))

	override suspend fun executeOnBackground(params: Flow<List<EvaluationFilter>>): Flow<GetEvaluations> {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluations(uid = activeUId)
			.combine(params) { evaluations, activeFilters ->
				val availableFilters = evaluations.computeAvailableFilters(resourceResolver)
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