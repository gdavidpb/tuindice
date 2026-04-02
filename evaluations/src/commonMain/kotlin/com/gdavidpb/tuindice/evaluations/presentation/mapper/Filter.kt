package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterChipItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterGroupItem

fun List<EvaluationFilter>.toEvaluationFilterGroupItemList(
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterGroupItem> {
	return this
		.groupBy { filter -> filter::class }
		.map { (_, filters) ->
			EvaluationFilterGroupItem(
				items = filters.map { filter ->
					filter.toEvaluationFilterChipItem(
						isChecked = activeFilters.contains(filter)
					)
				}
			)
		}
		.toList()
}

fun EvaluationFilter.toEvaluationFilterChipItem(
	isChecked: Boolean
): EvaluationFilterChipItem {
	return when (this) {
		is EvaluationSubjectFilter ->
			SubjectColorGenerator.fromCode(getLabel()).let { subjectColors ->
				EvaluationFilterChipItem(
					filter = this,
					labelText = getLabel(),
					isChecked = isChecked,
					contentColor = subjectColors.color,
					containerColor = subjectColors.containerColor,
					emphasizeWhenChecked = true
				)
			}

		else ->
			EvaluationFilterChipItem(
				filter = this,
				labelText = getLabel(),
				isChecked = isChecked
			)
	}
}

fun List<EvaluationFilterGroupItem>.availableFilters(): List<EvaluationFilter> {
	return flatMap { group -> group.items.map { item -> item.filter } }
}

fun List<EvaluationFilterGroupItem>.withActiveFilters(
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterGroupItem> {
	return availableFilters().toEvaluationFilterGroupItemList(activeFilters = activeFilters)
}
