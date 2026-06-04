package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

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
		val groups = weeklyGroups[weekItem.key]
			?.filter { group -> group.items.isNotEmpty() }
			.orEmpty()

		if (groups.isEmpty()) {
			return@mapNotNull null
		}

		EvaluationsWeekGroupItem(
			key = weekItem.key,
			title = weekItem.labelText,
			groups = groups
		)
	}
}

private fun List<Evaluation>.toWeeklyEvaluationGroups(
	currentTerm: EvaluationTermDescriptor?,
	attempts: List<EditableAttemptDescriptor>,
	mapping: EvaluationItemMapping
): Map<EvaluationsWeekKey, List<EvaluationsGroupItem>> {
	val continuousGroups = filter { evaluation ->
		evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS
	}.toEvaluationItemList(
		mapping = mapping,
		attempts = attempts
	)
	val weeklyGroups = (MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK).associate { weekNumber ->
		EvaluationsWeekKey.Academic(weekNumber) to filter { evaluation ->
			evaluation.academicWeekNumber(currentTerm) == weekNumber
		}
			.toEvaluationItemList(
				mapping = mapping,
				attempts = attempts
			)
	}

	return mapOf(EvaluationsWeekKey.Continuous to continuousGroups) + weeklyGroups
}
