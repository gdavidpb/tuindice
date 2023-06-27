package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class GetEvaluationsUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<Unit, List<Evaluation>, EvaluationsError>() {

	private val evaluationComparator = compareBy(
		Evaluation::isCompleted,
		Evaluation::isNotGraded,
		Evaluation::date
	)

	override suspend fun executeOnBackground(params: Unit): Flow<List<Evaluation>> {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluations(uid = activeUId)
			.transform { evaluations ->
				val sortedEvaluations = evaluations.sortedWith(evaluationComparator)

				emit(sortedEvaluations)
			}
	}
}