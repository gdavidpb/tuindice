package com.gdavidpb.tuindice.evaluations.domain.usecase

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.School
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilterEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetFiltersUseCase(
	private val resourceResolver: ResourceResolver
) : FlowUseCase<Map<String, List<Evaluation>>, List<EvaluationFilter>, Nothing>() {
	override suspend fun executeOnBackground(params: Map<String, List<Evaluation>>): Flow<List<EvaluationFilter>> {
		val stateName = resourceResolver.getString(R.string.label_category_state)
		val subjectName = resourceResolver.getString(R.string.label_category_subject)
		val dateName = resourceResolver.getString(R.string.label_category_date)

		val statePendingKey = resourceResolver.getString(R.string.label_state_pending)
		val stateCompletedKey = resourceResolver.getString(R.string.label_state_completed)
		val stateNoGradeKey = resourceResolver.getString(R.string.label_state_no_grade)

		val stateFilter = EvaluationFilter(
			name = stateName,
			icon = Icons.Filled.Category,
			entries = listOf(
				EvaluationFilterEntry(key = statePendingKey, isSelected = false),
				EvaluationFilterEntry(key = stateCompletedKey, isSelected = false),
				EvaluationFilterEntry(key = stateNoGradeKey, isSelected = false)
			)
		)

		val subjectFilter = params
			.values
			.flatten()
			.map { evaluation -> evaluation.subjectCode }
			.distinct()
			.map { subject ->
				EvaluationFilterEntry(
					key = subject,
					isSelected = false
				)
			}
			.let { entries ->
				EvaluationFilter(
					name = subjectName,
					icon = Icons.Filled.School,
					entries = entries
				)
			}

		val dateFilter = params
			.keys
			.map { date ->
				EvaluationFilterEntry(
					key = date,
					isSelected = false
				)
			}
			.let { entries ->
				EvaluationFilter(
					name = dateName,
					icon = Icons.Filled.CalendarMonth,
					entries = entries
				)
			}

		return flowOf(
			listOf(
				stateFilter,
				subjectFilter,
				dateFilter
			)
		)
	}
}