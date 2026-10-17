package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationOrdinalEngine
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

fun List<Evaluation>.toEvaluationItemList(
	mapping: EvaluationItemMapping,
	attempts: List<EditableAttemptDescriptor>
): List<EvaluationsGroupItem> {
	val attemptsById = attempts.associateBy(EditableAttemptDescriptor::id)
	// Filtered before grouping, so no date group is left with a header and nothing under it.
	val listedEvaluations = listedUnder(attempts)
	val ordinalsById = EvaluationOrdinalEngine.ordinalsById(listedEvaluations)

	return listedEvaluations
		.sortedWith(
			compareBy<Evaluation> { evaluation -> evaluation.date == null }
				.thenBy { evaluation -> evaluation.date ?: Long.MAX_VALUE }
		)
		.groupBy { evaluation -> evaluation.date?.toLocalDate() }
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
	val statusText = mapping.statusLabel(state)
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
		statusText = statusText,
		statusTone = mapping.highlightTone(state),
		typeText = mapping.typeLabel(type),
		typeNameText = typeName,
		typeIcon = mapping.typeIcon(type),
		dateText = mapping.dateText(this),
		dateIcon = mapping.dateIcon(state),
		place = inheritedEvaluationPlace(
			attemptMeetings = attempt.meetings,
			scheduleMode = scheduleMode,
			date = date?.toLocalDate()
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
		isClickable = true,
		assistantContext = assistantContextOf(
			titleText = typeName,
			gradeText = gradeText,
			statusText = statusText
		)
	)
}

// The context a question about this row is asked with: the evaluation, its subject and its attempt, and the texts
// the row shows as plain text. The date is ISO because its localized label only resolves under composition, and a
// continuous evaluation shows its mode where a dated one shows its date.
private fun Evaluation.assistantContextOf(
	titleText: String,
	gradeText: String,
	statusText: String
) = AssistantScreenContext(
	screen = AssistantScreen.EVALUATION_DETAIL,
	entities = listOf(
		AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = id),
		AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = subjectCode),
		AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = attemptId)
	),
	visibleFields = buildMap<String, String> {
		put("title", titleText)

		if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) {
			put("mode", scheduleMode.name)
		} else {
			date?.let { epochMillis -> put("date", epochMillis.toLocalDate().toString()) }
		}

		put("points", gradeText)
		grade?.let { value -> put("grade", value.formatGrade(decimals = 2)) }
		put("maxGrade", maxGrade.formatGrade(decimals = 2))
		put("state", statusText)
	}
)
