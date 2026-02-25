package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetAvailableSubjectsUseCase(
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<Unit, List<Subject>, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Subject>> {
		val availableSubjects = evaluationRepository
			.getAvailableSubjects()

		return flowOf(availableSubjects)
	}
}