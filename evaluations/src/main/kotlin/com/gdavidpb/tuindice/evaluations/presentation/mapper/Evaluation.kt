package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.AssignmentLate
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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

@Composable
@ReadOnlyComposable
fun List<Evaluation>.toEvaluationGroupItem() = groupBy { evaluation ->
	evaluation.date.formatAsToNow()
}.map { (title, evaluations) ->
	EvaluationsGroupItem(
		title = title,
		items = evaluations.map { evaluation -> evaluation.toEvaluationItem() }
	)
}

@Composable
@ReadOnlyComposable
fun Evaluation.toEvaluationItem() = EvaluationItem(
	evaluationId = evaluationId,
	name = stringResource(
		id = R.string.evaluation_name,
		type.asString(),
		ordinal
	),
	subjectCode = subject.code,
	grade = grade,
	maxGrade = maxGrade,
	highlightIconColor = when {
		isCompleted -> MaterialTheme.colorScheme.primary
		isGradeRequired -> MaterialTheme.colorScheme.error
		else -> MaterialTheme.colorScheme.outline
	},
	highlightTextColor = when {
		isGradeRequired -> MaterialTheme.colorScheme.error
		else -> Color.Unspecified
	},
	typeAndSubjectCode = stringResource(
		id = R.string.evaluation_title,
		type.asString(),
		subject.code
	),
	typeIcon = type.asIcon(),
	date = date.formatAsDayOfWeekAndDate(),
	dateIcon = when {
		isCompleted -> Icons.Outlined.EventAvailable
		isContinuous -> Icons.Outlined.EventRepeat
		else -> Icons.Outlined.Event
	},
	grades = if (!isGradeRequired)
		stringResource(
			id = R.string.evaluation_grade,
			grade,
			maxGrade
		)
	else
		stringResource(
			id = R.string.evaluation_not_completed
		),
	gradesIcon = if (!isGradeRequired)
		Icons.Outlined.AssignmentTurnedIn
	else
		Icons.Outlined.AssignmentLate,
	isDone = (isCompleted || isContinuous || isGradeRequired),
	isGradeRequired = isGradeRequired
)

@Composable
@ReadOnlyComposable
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

@Composable
@ReadOnlyComposable
fun EvaluationType.asString() = when (this) {
	EvaluationType.TEST -> R.string.evaluation_test
	EvaluationType.ESSAY -> R.string.evaluation_essay
	EvaluationType.ATTENDANCE -> R.string.evaluation_attendance
	EvaluationType.INTERVENTIONS -> R.string.evaluation_interventions
	EvaluationType.LABORATORY -> R.string.evaluation_laboratory
	EvaluationType.MODEL -> R.string.evaluation_model
	EvaluationType.PRESENTATION -> R.string.evaluation_presentation
	EvaluationType.PROJECT -> R.string.evaluation_project
	EvaluationType.QUIZ -> R.string.evaluation_quiz
	EvaluationType.REPORT -> R.string.evaluation_report
	EvaluationType.WORKSHOP -> R.string.evaluation_workshop
	EvaluationType.WRITTEN_WORK -> R.string.evaluation_written_work
	EvaluationType.OTHER -> R.string.evaluation_other
}.let { resId -> stringResource(id = resId) }

@StringRes
@Deprecated("This will be removed.", ReplaceWith("EvaluationType.stringRes"))
fun EvaluationType.stringRes() = when (this) {
	EvaluationType.TEST -> R.string.evaluation_test
	EvaluationType.ESSAY -> R.string.evaluation_essay
	EvaluationType.ATTENDANCE -> R.string.evaluation_attendance
	EvaluationType.INTERVENTIONS -> R.string.evaluation_interventions
	EvaluationType.LABORATORY -> R.string.evaluation_laboratory
	EvaluationType.MODEL -> R.string.evaluation_model
	EvaluationType.PRESENTATION -> R.string.evaluation_presentation
	EvaluationType.PROJECT -> R.string.evaluation_project
	EvaluationType.QUIZ -> R.string.evaluation_quiz
	EvaluationType.REPORT -> R.string.evaluation_report
	EvaluationType.WORKSHOP -> R.string.evaluation_workshop
	EvaluationType.WRITTEN_WORK -> R.string.evaluation_written_work
	EvaluationType.OTHER -> R.string.evaluation_other
}

@Deprecated("This will be removed.", ReplaceWith("EvaluationType.asIcon"))
fun EvaluationType.iconRes() = when (this) {
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
