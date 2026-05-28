package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.AssignmentReturned
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_attendance
import tuindice.evaluations.generated.resources.evaluation_essay
import tuindice.evaluations.generated.resources.evaluation_grade
import tuindice.evaluations.generated.resources.evaluation_interventions
import tuindice.evaluations.generated.resources.evaluation_laboratory
import tuindice.evaluations.generated.resources.evaluation_model
import tuindice.evaluations.generated.resources.evaluation_name
import tuindice.evaluations.generated.resources.evaluation_not_grade
import tuindice.evaluations.generated.resources.evaluation_other
import tuindice.evaluations.generated.resources.evaluation_pending_grade
import tuindice.evaluations.generated.resources.evaluation_presentation
import tuindice.evaluations.generated.resources.evaluation_project
import tuindice.evaluations.generated.resources.evaluation_quiz
import tuindice.evaluations.generated.resources.evaluation_report
import tuindice.evaluations.generated.resources.evaluation_test
import tuindice.evaluations.generated.resources.evaluation_workshop
import tuindice.evaluations.generated.resources.evaluation_written_work
import tuindice.evaluations.generated.resources.label_evaluation_assign_grade

data class EvaluationItemMapping(
	val evaluationName: (type: EvaluationType, ordinal: Int) -> String,
	val typeLabel: (type: EvaluationType) -> String,
	val gradesCompleted: (grade: Double?, maxGrade: Double) -> String,
	val gradesPending: (maxGrade: Double) -> String,
	val gradesOverdue: (maxGrade: Double) -> String,
	val gradeAction: (grade: Double?) -> String,
	val typeIcon: (type: EvaluationType) -> ImageVector,
	val dateIcon: (state: EvaluationState) -> ImageVector,
	val gradesIcon: (state: EvaluationState) -> ImageVector,
	val dateGroupTitle: (group: EvaluationDateGroup) -> String,
	val dateText: (evaluation: Evaluation) -> String,
	val highlightTone: (state: EvaluationState) -> EvaluationHighlightTone
)

@Composable
fun rememberEvaluationItemMapping(): EvaluationItemMapping {
	val dateTextMapping = rememberEvaluationDateTextMapping()

	val evaluationNamePattern = stringResource(Res.string.evaluation_name)
	val evaluationGradePattern = stringResource(Res.string.evaluation_grade)
	val evaluationPendingGradePattern = stringResource(Res.string.evaluation_pending_grade)
	val evaluationNotGradePattern = stringResource(Res.string.evaluation_not_grade)
	val evaluationAssignGradeLabel = stringResource(Res.string.label_evaluation_assign_grade)
	val typeLabels = rememberEvaluationTypeLabels()

	return remember(
		dateTextMapping,
		evaluationNamePattern,
		evaluationGradePattern,
		evaluationPendingGradePattern,
		evaluationNotGradePattern,
		evaluationAssignGradeLabel,
		typeLabels
	) {
		buildEvaluationItemMapping(
			dateTextMapping = dateTextMapping,
			evaluationNamePattern = evaluationNamePattern,
			evaluationGradePattern = evaluationGradePattern,
			evaluationPendingGradePattern = evaluationPendingGradePattern,
			evaluationNotGradePattern = evaluationNotGradePattern,
			evaluationAssignGradeLabel = evaluationAssignGradeLabel,
			typeLabels = typeLabels
		)
	}
}

suspend fun getEvaluationItemMapping(): EvaluationItemMapping {
	return buildEvaluationItemMapping(
		dateTextMapping = getEvaluationDateTextMapping(),
		evaluationNamePattern = getString(Res.string.evaluation_name),
		evaluationGradePattern = getString(Res.string.evaluation_grade),
		evaluationPendingGradePattern = getString(Res.string.evaluation_pending_grade),
		evaluationNotGradePattern = getString(Res.string.evaluation_not_grade),
		evaluationAssignGradeLabel = getString(Res.string.label_evaluation_assign_grade),
		typeLabels = getEvaluationTypeLabels()
	)
}

private fun buildEvaluationItemMapping(
	dateTextMapping: EvaluationDateTextMapping,
	evaluationNamePattern: String,
	evaluationGradePattern: String,
	evaluationPendingGradePattern: String,
	evaluationNotGradePattern: String,
	evaluationAssignGradeLabel: String,
	typeLabels: Map<EvaluationType, String>
): EvaluationItemMapping {
	return EvaluationItemMapping(
		evaluationName = { type, ordinal ->
			evaluationNamePattern
				.replace("%1${'$'}s", type.asString(typeLabels))
				.replace("%2${'$'}d", ordinal.toString())
		},
		typeLabel = { type -> type.asString(typeLabels) },
		gradesCompleted = { grade, maxGrade ->
			evaluationGradePattern
				.replace("%1${'$'}.2f", (grade ?: 0.0).formatGrade(decimals = 2))
				.replace("%2${'$'}.2f", maxGrade.formatGrade(decimals = 2))
		},
		gradesPending = { maxGrade ->
			evaluationPendingGradePattern
				.replace("%1${'$'}.2f", maxGrade.formatGrade(decimals = 2))
		},
		gradesOverdue = { maxGrade ->
			evaluationNotGradePattern
				.replace("%1${'$'}.2f", maxGrade.formatGrade(decimals = 2))
		},
		gradeAction = { grade ->
			grade?.formatGrade(decimals = 2) ?: evaluationAssignGradeLabel
		},
		typeIcon = { type -> type.asIcon() },
		dateIcon = { state ->
			when (state) {
				EvaluationState.COMPLETED -> Icons.Outlined.EventAvailable
				EvaluationState.CONTINUOUS -> Icons.Outlined.EventRepeat
				else -> Icons.Outlined.Event
			}
		},
		gradesIcon = { state ->
			when (state) {
				EvaluationState.COMPLETED, EvaluationState.CONTINUOUS -> Icons.Outlined.AssignmentTurnedIn
				EvaluationState.PENDING -> Icons.Outlined.AssignmentReturned
				EvaluationState.OVERDUE -> Icons.Outlined.AssignmentLate
			}
		},
		dateGroupTitle = { bucket ->
			bucket.getLabel(dateTextMapping)
		},
		dateText = { evaluation: Evaluation ->
			evaluation.formatAsDayOfWeekAndDate(noDateLabel = dateTextMapping.noDateLabel)
		},
		highlightTone = { state ->
			when (state) {
				EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
				EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
				else -> EvaluationHighlightTone.Neutral
			}
		}
	)
}

fun EvaluationType.asIcon() = when (this) {
	EvaluationType.TEST -> Icons.Outlined.FileCopy
	EvaluationType.ESSAY -> Icons.Outlined.HistoryEdu
	EvaluationType.ATTENDANCE -> Icons.Outlined.BackHand
	EvaluationType.INTERVENTIONS -> Icons.Outlined.ModeComment
	EvaluationType.LABORATORY -> Icons.Outlined.Science
	EvaluationType.MODEL -> Icons.Outlined.Apartment
	EvaluationType.PRESENTATION -> Icons.Outlined.CoPresent
	EvaluationType.PROJECT -> Icons.Outlined.AccountTree
	EvaluationType.QUIZ -> Icons.Outlined.Quiz
	EvaluationType.REPORT -> Icons.Outlined.AreaChart
	EvaluationType.WORKSHOP -> Icons.Outlined.Build
	EvaluationType.WRITTEN_WORK -> Icons.Outlined.Edit
	EvaluationType.OTHER -> Icons.Outlined.Tag
}

private fun EvaluationType.asString(typeLabels: Map<EvaluationType, String>) =
	typeLabels.getValue(this)

@Composable
fun EvaluationType.asString() = when (this) {
	EvaluationType.TEST -> stringResource(Res.string.evaluation_test)
	EvaluationType.ESSAY -> stringResource(Res.string.evaluation_essay)
	EvaluationType.ATTENDANCE -> stringResource(Res.string.evaluation_attendance)
	EvaluationType.INTERVENTIONS -> stringResource(Res.string.evaluation_interventions)
	EvaluationType.LABORATORY -> stringResource(Res.string.evaluation_laboratory)
	EvaluationType.MODEL -> stringResource(Res.string.evaluation_model)
	EvaluationType.PRESENTATION -> stringResource(Res.string.evaluation_presentation)
	EvaluationType.PROJECT -> stringResource(Res.string.evaluation_project)
	EvaluationType.QUIZ -> stringResource(Res.string.evaluation_quiz)
	EvaluationType.REPORT -> stringResource(Res.string.evaluation_report)
	EvaluationType.WORKSHOP -> stringResource(Res.string.evaluation_workshop)
	EvaluationType.WRITTEN_WORK -> stringResource(Res.string.evaluation_written_work)
	EvaluationType.OTHER -> stringResource(Res.string.evaluation_other)
}
