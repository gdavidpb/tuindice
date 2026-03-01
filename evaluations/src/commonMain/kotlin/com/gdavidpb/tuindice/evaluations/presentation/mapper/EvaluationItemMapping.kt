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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
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
import tuindice.evaluations.generated.resources.evaluation_title
import tuindice.evaluations.generated.resources.evaluation_workshop
import tuindice.evaluations.generated.resources.evaluation_written_work

@Composable
fun rememberEvaluationItemMapping(): EvaluationItemMapping {
	val colorScheme = MaterialTheme.colorScheme

	val evaluationNamePattern = stringResource(Res.string.evaluation_name)
	val evaluationTitlePattern = stringResource(Res.string.evaluation_title)
	val evaluationGradePattern = stringResource(Res.string.evaluation_grade)
	val evaluationPendingGradePattern = stringResource(Res.string.evaluation_pending_grade)
	val evaluationNotGradePattern = stringResource(Res.string.evaluation_not_grade)

	val testLabel = stringResource(Res.string.evaluation_test)
	val essayLabel = stringResource(Res.string.evaluation_essay)
	val attendanceLabel = stringResource(Res.string.evaluation_attendance)
	val interventionsLabel = stringResource(Res.string.evaluation_interventions)
	val laboratoryLabel = stringResource(Res.string.evaluation_laboratory)
	val modelLabel = stringResource(Res.string.evaluation_model)
	val presentationLabel = stringResource(Res.string.evaluation_presentation)
	val projectLabel = stringResource(Res.string.evaluation_project)
	val quizLabel = stringResource(Res.string.evaluation_quiz)
	val reportLabel = stringResource(Res.string.evaluation_report)
	val workshopLabel = stringResource(Res.string.evaluation_workshop)
	val writtenWorkLabel = stringResource(Res.string.evaluation_written_work)
	val otherLabel = stringResource(Res.string.evaluation_other)

	val typeLabels = remember(
		testLabel,
		essayLabel,
		attendanceLabel,
		interventionsLabel,
		laboratoryLabel,
		modelLabel,
		presentationLabel,
		projectLabel,
		quizLabel,
		reportLabel,
		workshopLabel,
		writtenWorkLabel,
		otherLabel
	) {
		mapOf(
			EvaluationType.TEST to testLabel,
			EvaluationType.ESSAY to essayLabel,
			EvaluationType.ATTENDANCE to attendanceLabel,
			EvaluationType.INTERVENTIONS to interventionsLabel,
			EvaluationType.LABORATORY to laboratoryLabel,
			EvaluationType.MODEL to modelLabel,
			EvaluationType.PRESENTATION to presentationLabel,
			EvaluationType.PROJECT to projectLabel,
			EvaluationType.QUIZ to quizLabel,
			EvaluationType.REPORT to reportLabel,
			EvaluationType.WORKSHOP to workshopLabel,
			EvaluationType.WRITTEN_WORK to writtenWorkLabel,
			EvaluationType.OTHER to otherLabel
		)
	}

	return EvaluationItemMapping(
		evaluationName = { type, ordinal ->
			evaluationNamePattern
				.replace("%1${'$'}s", type.asString(typeLabels))
				.replace("%2${'$'}d", ordinal.toString())
		},
		evaluationTitle = { type, subjectCode ->
			evaluationTitlePattern
				.replace("%1${'$'}s", type.asString(typeLabels))
				.replace("%2${'$'}s", subjectCode)
		},
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
		highlightIconColor = { state ->
			when (state) {
				EvaluationState.COMPLETED -> colorScheme.primary
				EvaluationState.OVERDUE -> colorScheme.error
				else -> colorScheme.outline
			}
		},
		highlightTextColor = { state ->
			when (state) {
				EvaluationState.OVERDUE -> colorScheme.error
				else -> Color.Unspecified
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
