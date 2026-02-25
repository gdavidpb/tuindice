package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import org.koin.compose.koinInject

data class EvaluationItemMapping(
	val evaluationName: (type: EvaluationType, ordinal: Int) -> String,
	val evaluationTitle: (type: EvaluationType, subjectCode: String) -> String,
	val gradesCompleted: (grade: Double?, maxGrade: Double) -> String,
	val gradesPending: (maxGrade: Double) -> String,
	val gradesOverdue: (maxGrade: Double) -> String,
	val typeIcon: (type: EvaluationType) -> ImageVector,
	val dateIcon: (state: EvaluationState) -> ImageVector,
	val gradesIcon: (state: EvaluationState) -> ImageVector,
	val highlightIconColor: (state: EvaluationState) -> Color,
	val highlightTextColor: (state: EvaluationState) -> Color
)

@Composable
fun List<Evaluation>.toEvaluationItemList(
	mappingProvider: EvaluationItemMappingProvider = koinInject()
): List<EvaluationsGroupItem> {
	return toEvaluationItemList(mapping = mappingProvider.rememberMapping())
}

fun List<Evaluation>.toEvaluationItemList(
	mapping: EvaluationItemMapping
): List<EvaluationsGroupItem> {
	val ordinalsById =
		sortedBy { evaluation -> evaluation.date }
			.groupBy { evaluation -> evaluation.subjectId to evaluation.type }
			.flatMap { (_, evaluations) ->
				evaluations
					.mapIndexed { index, evaluation ->
						evaluation.id to index + 1
					}
			}.toMap()

	return groupBy { evaluation -> evaluation.date.formatAsToNow() }
		.map { (title, evaluations) ->
			EvaluationsGroupItem(
				title = title,
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
) = EvaluationItem(
	evaluationId = id,
	grade = grade,
	maxGrade = maxGrade,
	nameText = mapping.evaluationName(type, ordinal),
	subjectCodeText = subjectCode,
	highlightIconColor = mapping.highlightIconColor(state),
	highlightTextColor = mapping.highlightTextColor(state),
	typeAndSubjectCodeText = mapping.evaluationTitle(type, subjectCode),
	typeIcon = mapping.typeIcon(type),
	dateText = date.formatAsDayOfWeekAndDate(),
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
