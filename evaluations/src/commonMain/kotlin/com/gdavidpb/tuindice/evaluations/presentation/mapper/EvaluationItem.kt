package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationLocalDate

fun List<Evaluation>.toEvaluationItemList(
	mapping: EvaluationItemMapping,
	attempts: List<EditableAttemptDescriptor>
): List<EvaluationsGroupItem> {
	val attemptsById = attempts.associateBy(EditableAttemptDescriptor::id)
	// Filtered before grouping, so no date group is left with a header and nothing under it.
	val listedEvaluations = listedUnder(attempts)
	val ordinalsById = listedEvaluations
		.sortedBy { evaluation -> evaluation.date }
		.groupBy { evaluation -> evaluation.attemptId to evaluation.type }
		.flatMap { (_, evaluations) ->
			evaluations
				.mapIndexed { index, evaluation ->
					evaluation.id to index + 1
				}
		}.toMap()

	return listedEvaluations
		.sortedWith(
			compareBy<Evaluation> { evaluation -> evaluation.date == null }
				.thenBy { evaluation -> evaluation.date ?: Long.MAX_VALUE }
		)
		.groupBy { evaluation -> evaluation.date?.toEvaluationLocalDate() }
		.map { (_, evaluations) ->
			EvaluationsGroupItem(
				items = evaluations.mapNotNull { evaluation ->
					attemptsById[evaluation.attemptId]?.let { attempt ->
						evaluation.toEvaluationItem(
							ordinal = ordinalsById[evaluation.id] ?: 1,
							mapping = mapping,
							attempt = attempt
						)
					}
				}
			)
		}
}

fun Evaluation.toEvaluationItem(
	ordinal: Int,
	mapping: EvaluationItemMapping,
	attempt: EditableAttemptDescriptor
) = CourseCodeColorGenerator.fromCode(subjectCode).let { subjectColors ->
	val typeName = mapping.evaluationName(type, ordinal).uppercase()
	val gradeText = mapping.scoreGrade(grade, maxGrade)
	EvaluationItem(
		evaluationId = id,
		grade = grade,
		maxGrade = maxGrade,
		nameText = typeName,
		subjectNameText = attempt.name,
		subjectCodeText = subjectCode,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = mapping.highlightTone(state),
		statusText = mapping.statusLabel(state),
		statusTone = mapping.highlightTone(state),
		typeText = mapping.typeLabel(type),
		typeNameText = typeName,
		typeIcon = mapping.typeIcon(type),
		dateText = mapping.dateText(this),
		dateIcon = mapping.dateIcon(state),
		place = inheritedEvaluationPlace(
			attemptMeetings = attempt.meetings,
			scheduleMode = scheduleMode,
			date = date?.toEvaluationLocalDate()
		),
		gradeText = gradeText,
		gradesText = when (state) {
			EvaluationState.COMPLETED, EvaluationState.CONTINUOUS ->
				mapping.gradesCompleted(grade, maxGrade)

			EvaluationState.PENDING ->
				mapping.gradesPending(maxGrade)

			EvaluationState.OVERDUE ->
				mapping.gradesOverdue(maxGrade)
		},
		gradeActionText = gradeText,
		showsGradeAction = true,
		gradesIcon = mapping.gradesIcon(state),
		isOverdue = (state == EvaluationState.OVERDUE),
		isClickable = true
	)
}
