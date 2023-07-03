package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSectionItem

@Composable
fun rememberEvaluationFilters(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterSectionItem> {
	val state = stringResource(id = R.string.label_category_state)
	val subject = stringResource(id = R.string.label_category_subject)
	val date = stringResource(id = R.string.label_category_date)

	return remember {
		availableFilters.groupBy { filter -> filter::class }
			.map { (clazz, filters) ->
				when (clazz) {
					EvaluationStateFilter::class ->
						EvaluationFilterSectionItem(
							name = state,
							icon = Icons.Outlined.Category,
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationSubjectFilter::class ->
						EvaluationFilterSectionItem(
							name = subject,
							icon = Icons.Outlined.School,
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationDateFilter::class ->
						EvaluationFilterSectionItem(
							name = date,
							icon = Icons.Outlined.CalendarMonth,
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					else -> throw NoWhenBranchMatchedException()
				}
			}
	}
}

