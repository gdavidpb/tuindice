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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_attendance
import tuindice.evaluations.generated.resources.evaluation_essay
import tuindice.evaluations.generated.resources.evaluation_empty_grade
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
import tuindice.evaluations.generated.resources.evaluation_score_grade
import tuindice.evaluations.generated.resources.evaluation_status_completed
import tuindice.evaluations.generated.resources.evaluation_status_continuous
import tuindice.evaluations.generated.resources.evaluation_status_pending
import tuindice.evaluations.generated.resources.evaluation_status_scheduled
import tuindice.evaluations.generated.resources.evaluation_test
import tuindice.evaluations.generated.resources.evaluation_workshop
import tuindice.evaluations.generated.resources.evaluation_written_work

data class EvaluationItemMapping(
	val evaluationName: (type: EvaluationType, ordinal: Int) -> String,
	val typeLabel: (type: EvaluationType) -> String,
	val gradesCompleted: (grade: Double?, maxGrade: Double) -> String,
	val gradesPending: (maxGrade: Double) -> String,
	val gradesOverdue: (maxGrade: Double) -> String,
	val scoreGrade: (grade: Double?, maxGrade: Double) -> String,
	val statusLabel: (state: EvaluationState) -> String,
	val typeIcon: (type: EvaluationType) -> ImageVector,
	val dateIcon: (state: EvaluationState) -> ImageVector,
	val gradesIcon: (state: EvaluationState) -> ImageVector,
	val dateGroupTitle: (group: EvaluationDateGroup) -> String,
	val dateHeaderText: (evaluation: Evaluation) -> String,
	val dateText: (evaluation: Evaluation) -> String,
	val highlightTone: (state: EvaluationState) -> EvaluationHighlightTone
)

@Composable
fun rememberEvaluationItemMapping(): EvaluationItemMapping {
	val dateTextMapping = rememberEvaluationDateTextMapping()

	val evaluationNamePattern = stringResource(Res.string.evaluation_name)
	val evaluationGradePattern = stringResource(Res.string.evaluation_grade)
	val evaluationScoreGradePattern = stringResource(Res.string.evaluation_score_grade)
	val evaluationEmptyGradeLabel = stringResource(Res.string.evaluation_empty_grade)
	val evaluationPendingGradePattern = stringResource(Res.string.evaluation_pending_grade)
	val evaluationNotGradePattern = stringResource(Res.string.evaluation_not_grade)
	val evaluationPendingStatusLabel = stringResource(Res.string.evaluation_status_pending)
	val evaluationScheduledStatusLabel = stringResource(Res.string.evaluation_status_scheduled)
	val evaluationCompletedStatusLabel = stringResource(Res.string.evaluation_status_completed)
	val evaluationContinuousStatusLabel = stringResource(Res.string.evaluation_status_continuous)
	val typeLabels = rememberEvaluationTypeLabels()

	return remember(
		dateTextMapping,
		evaluationNamePattern,
		evaluationGradePattern,
		evaluationScoreGradePattern,
		evaluationEmptyGradeLabel,
		evaluationPendingGradePattern,
		evaluationNotGradePattern,
		evaluationPendingStatusLabel,
		evaluationScheduledStatusLabel,
		evaluationCompletedStatusLabel,
		evaluationContinuousStatusLabel,
		typeLabels
	) {
		buildEvaluationItemMapping(
			dateTextMapping = dateTextMapping,
			evaluationNamePattern = evaluationNamePattern,
			evaluationGradePattern = evaluationGradePattern,
			evaluationScoreGradePattern = evaluationScoreGradePattern,
			evaluationEmptyGradeLabel = evaluationEmptyGradeLabel,
			evaluationPendingGradePattern = evaluationPendingGradePattern,
			evaluationNotGradePattern = evaluationNotGradePattern,
			evaluationPendingStatusLabel = evaluationPendingStatusLabel,
			evaluationScheduledStatusLabel = evaluationScheduledStatusLabel,
			evaluationCompletedStatusLabel = evaluationCompletedStatusLabel,
			evaluationContinuousStatusLabel = evaluationContinuousStatusLabel,
			typeLabels = typeLabels
		)
	}
}

suspend fun getEvaluationItemMapping(): EvaluationItemMapping {
	return buildEvaluationItemMapping(
		dateTextMapping = getEvaluationDateTextMapping(),
		evaluationNamePattern = getString(Res.string.evaluation_name),
		evaluationGradePattern = getString(Res.string.evaluation_grade),
		evaluationScoreGradePattern = getString(Res.string.evaluation_score_grade),
		evaluationEmptyGradeLabel = getString(Res.string.evaluation_empty_grade),
		evaluationPendingGradePattern = getString(Res.string.evaluation_pending_grade),
		evaluationNotGradePattern = getString(Res.string.evaluation_not_grade),
		evaluationPendingStatusLabel = getString(Res.string.evaluation_status_pending),
		evaluationScheduledStatusLabel = getString(Res.string.evaluation_status_scheduled),
		evaluationCompletedStatusLabel = getString(Res.string.evaluation_status_completed),
		evaluationContinuousStatusLabel = getString(Res.string.evaluation_status_continuous),
		typeLabels = getEvaluationTypeLabels()
	)
}

private fun buildEvaluationItemMapping(
	dateTextMapping: EvaluationDateTextMapping,
	evaluationNamePattern: String,
	evaluationGradePattern: String,
	evaluationScoreGradePattern: String,
	evaluationEmptyGradeLabel: String,
	evaluationPendingGradePattern: String,
	evaluationNotGradePattern: String,
	evaluationPendingStatusLabel: String,
	evaluationScheduledStatusLabel: String,
	evaluationCompletedStatusLabel: String,
	evaluationContinuousStatusLabel: String,
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
		scoreGrade = { grade, maxGrade ->
			val maxGradeText = maxGrade.formatCompactGrade()

			grade?.let { value ->
				evaluationScoreGradePattern
					.replace("%1${'$'}s", value.formatCompactGrade())
					.replace("%2${'$'}s", maxGradeText)
			} ?: evaluationEmptyGradeLabel.replace("%1${'$'}s", maxGradeText)
		},
		statusLabel = { state ->
			when (state) {
				EvaluationState.PENDING -> evaluationScheduledStatusLabel
				EvaluationState.OVERDUE -> evaluationPendingStatusLabel
				EvaluationState.COMPLETED -> evaluationCompletedStatusLabel
				EvaluationState.CONTINUOUS -> evaluationContinuousStatusLabel
			}
		},
		typeIcon = { type -> type.asIcon() },
		dateIcon = { Icons.Outlined.CalendarToday },
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
		dateHeaderText = { evaluation: Evaluation ->
			evaluation.formatAsExactDateHeader(noDateLabel = dateTextMapping.noDateLabel)
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

private fun Double.formatCompactGrade(): String {
	return if (this == toInt().toDouble()) {
		formatGrade(decimals = 0)
	} else {
		formatGrade(decimals = 2)
	}
}

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
