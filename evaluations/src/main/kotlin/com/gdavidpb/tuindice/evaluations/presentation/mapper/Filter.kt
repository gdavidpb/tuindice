package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSectionItem

@Composable
fun Collection<EvaluationFilter>.rememberEvaluationFilters() =
	groupBy { filter -> filter::class }
		.map { (clazz, filters) ->
			when (clazz) {
				EvaluationStateFilter::class ->
					EvaluationFilterSectionItem(
						name = stringResource(id = R.string.label_category_state),
						icon = Icons.Outlined.Category,
						entries = filters.associateWith { mutableStateOf(false) }
					)

				EvaluationSubjectFilter::class ->
					EvaluationFilterSectionItem(
						name = stringResource(id = R.string.label_category_subject),
						icon = Icons.Outlined.School,
						entries = filters.associateWith { mutableStateOf(false) }
					)

				EvaluationDateFilter::class ->
					EvaluationFilterSectionItem(
						name = stringResource(id = R.string.label_category_date),
						icon = Icons.Outlined.CalendarMonth,
						entries = filters.associateWith { mutableStateOf(false) }
					)

				else -> throw NoWhenBranchMatchedException()
			}
		}
