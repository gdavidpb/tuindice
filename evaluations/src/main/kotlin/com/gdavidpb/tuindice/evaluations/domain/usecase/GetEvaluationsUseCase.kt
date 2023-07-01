package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class GetEvaluationsUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<List<EvaluationFilter>, List<Evaluation>, EvaluationsError>() {

	private val evaluationComparator = compareBy(
		Evaluation::isCompleted,
		Evaluation::isNotGraded,
		Evaluation::date
	)

	override suspend fun executeOnBackground(params: List<EvaluationFilter>): Flow<List<Evaluation>> {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluations(uid = activeUId)
			.transform { evaluations ->
				val sortedEvaluations = evaluations.sortedWith(evaluationComparator)

				val filteredEvaluations = if (params.isEmpty())
					sortedEvaluations
				else
					params
						.groupBy { filter -> filter::class }
						.entries
						.fold(initial = sortedEvaluations) { acc, entry ->
							acc.filter { evaluation ->
								entry.value.any { filter -> filter.filter(evaluation) }
							}
						}

				emit(filteredEvaluations)
			}
	}
}