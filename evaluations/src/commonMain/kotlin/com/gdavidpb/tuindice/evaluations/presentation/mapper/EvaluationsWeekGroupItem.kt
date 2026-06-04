package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem

fun List<EvaluationsWeekItem>.toEvaluationsWeekGroupItemList(
	evaluations: List<Evaluation>,
	currentTerm: EvaluationTermDescriptor?,
	attempts: List<EditableAttemptDescriptor>,
	mapping: EvaluationItemMapping
): List<EvaluationsWeekGroupItem> {
	val weeklyGroups = evaluations.toWeeklyEvaluationGroups(
		currentTerm = currentTerm,
		attempts = attempts,
		mapping = mapping
	)

	return mapNotNull { weekItem ->
		val groups = weeklyGroups[weekItem.weekNumber]
			?.filter { group -> group.items.isNotEmpty() }
			.orEmpty()

		if (groups.isEmpty()) {
			return@mapNotNull null
		}

		EvaluationsWeekGroupItem(
			weekNumber = weekItem.weekNumber,
			title = weekItem.labelText,
			groups = groups
		)
	}
}

private fun List<Evaluation>.toWeeklyEvaluationGroups(
	currentTerm: EvaluationTermDescriptor?,
	attempts: List<EditableAttemptDescriptor>,
	mapping: EvaluationItemMapping
): Map<Int, List<EvaluationsGroupItem>> {
	return (MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK).associateWith { weekNumber ->
		filter { evaluation -> evaluation.academicWeekNumber(currentTerm) == weekNumber }
			.toEvaluationItemList(
				mapping = mapping,
				attempts = attempts
			)
	}
}
