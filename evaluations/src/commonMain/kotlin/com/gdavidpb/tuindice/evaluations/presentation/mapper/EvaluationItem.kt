package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

fun List<Evaluation>.toEvaluationItemList(
	mapping: EvaluationItemMapping
): List<EvaluationsGroupItem> {
	val ordinalsById =
		sortedBy { evaluation -> evaluation.date }
			.groupBy { evaluation -> evaluation.attemptId to evaluation.type }
			.flatMap { (_, evaluations) ->
				evaluations
					.mapIndexed { index, evaluation ->
						evaluation.id to index + 1
					}
			}.toMap()

	return groupBy { evaluation -> evaluation.toEvaluationDateGroup() }
		.map { (group, evaluations) ->
			EvaluationsGroupItem(
				title = mapping.dateGroupTitle(group),
				items = evaluations.map { evaluation ->
					evaluation.toEvaluationItem(
						ordinal = ordinalsById[evaluation.id] ?: 1,
						mapping = mapping
					)
				}
			)
		}
}

fun Evaluation.toEvaluationItem(
	ordinal: Int,
	mapping: EvaluationItemMapping
) = CourseCodeColorGenerator.fromCode(subjectCode).let { subjectColors ->
	EvaluationItem(
		evaluationId = id,
		grade = grade,
		maxGrade = maxGrade,
		nameText = mapping.evaluationName(type, ordinal),
		subjectCodeText = subjectCode,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = mapping.highlightTone(state),
		typeText = mapping.typeLabel(type),
		typeIcon = mapping.typeIcon(type),
		dateText = mapping.dateText(this),
		dateIcon = mapping.dateIcon(state),
		gradesText = when (state) {
			EvaluationState.COMPLETED, EvaluationState.CONTINUOUS ->
				mapping.gradesCompleted(grade, maxGrade)

			EvaluationState.PENDING ->
				mapping.gradesPending(maxGrade)

			EvaluationState.OVERDUE ->
				mapping.gradesOverdue(maxGrade)
		},
		gradesIcon = mapping.gradesIcon(state),
		isOverdue = (state == EvaluationState.OVERDUE),
		isClickable = (state != EvaluationState.PENDING)
	)
}
