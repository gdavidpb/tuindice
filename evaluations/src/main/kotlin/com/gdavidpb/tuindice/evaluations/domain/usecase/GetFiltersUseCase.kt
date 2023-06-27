package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetFiltersUseCase(
	private val resourceResolver: ResourceResolver
) : FlowUseCase<List<Evaluation>, List<EvaluationFilter>, Nothing>() {
	override suspend fun executeOnBackground(params: List<Evaluation>): Flow<List<EvaluationFilter>> {
		if (params.isEmpty()) return flowOf(emptyList())

		val statesFilters = listOf(
			EvaluationStateFilter(
				label = resourceResolver.getString(R.string.label_state_pending)
			) { evaluation -> !evaluation.isCompleted },
			EvaluationStateFilter(
				label = resourceResolver.getString(R.string.label_state_completed)
			) { evaluation -> evaluation.isCompleted },
			EvaluationStateFilter(
				label = resourceResolver.getString(R.string.label_state_not_grade)
			) { evaluation -> evaluation.isNotGraded }
		)

		val subjectsFilters = params
			.map { evaluation -> evaluation.subjectCode }
			.distinct()
			.map { subject -> EvaluationSubjectFilter(subject) }

		val datesFilters = params
			.map { evaluation -> evaluation.date.dateGroup() }
			.distinct()
			.map { date -> EvaluationDateFilter(date) }

		val filters = mutableListOf<EvaluationFilter>()

		filters.addAll(statesFilters)
		filters.addAll(subjectsFilters)
		filters.addAll(datesFilters)

		return flowOf(filters)
	}
}