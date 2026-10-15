package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.ScheduleBlockClock
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryClassMoment
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeekEvaluation
import com.gdavidpb.tuindice.summary.presentation.model.SummaryClassRowItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryWeekItem
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_class_in_progress
import tuindice.summary.generated.resources.summary_day_date
import tuindice.summary.generated.resources.summary_day_today
import tuindice.summary.generated.resources.summary_evaluation_name
import tuindice.summary.generated.resources.summary_evaluation_status_completed
import tuindice.summary.generated.resources.summary_evaluation_status_continuous
import tuindice.summary.generated.resources.summary_evaluation_status_overdue
import tuindice.summary.generated.resources.summary_evaluation_status_scheduled
import tuindice.summary.generated.resources.summary_evaluation_type_attendance
import tuindice.summary.generated.resources.summary_evaluation_type_essay
import tuindice.summary.generated.resources.summary_evaluation_type_interventions
import tuindice.summary.generated.resources.summary_evaluation_type_laboratory
import tuindice.summary.generated.resources.summary_evaluation_type_model
import tuindice.summary.generated.resources.summary_evaluation_type_other
import tuindice.summary.generated.resources.summary_evaluation_type_presentation
import tuindice.summary.generated.resources.summary_evaluation_type_project
import tuindice.summary.generated.resources.summary_evaluation_type_quiz
import tuindice.summary.generated.resources.summary_evaluation_type_report
import tuindice.summary.generated.resources.summary_evaluation_type_test
import tuindice.summary.generated.resources.summary_evaluation_type_workshop
import tuindice.summary.generated.resources.summary_evaluation_type_written_work

fun SummaryWeek.toSummaryWeekItem(): SummaryWeekItem {
	return when (this) {
		is SummaryWeek.InProgress -> toInProgressItem()

		is SummaryWeek.Idle -> SummaryWeekItem(
			classRow = null,
			evaluations = emptyList(),
			hasSchedule = false,
			card = if (hasTerms) noClassCard() else noTermCard()
		)
	}
}

// The rows of the week are its class and its evaluations; the card says what they leave out. Until the evaluations are
// downloaded the week says nothing of them, and with no evaluation registered in the term it invites to add one.
private fun SummaryWeek.InProgress.toInProgressItem(): SummaryWeekItem {
	val classRow = classMoment?.toSummaryClassRowItem()
	val evaluationRows = evaluations.map { evaluation -> evaluation.toSummaryEvaluationRowItem() }
	val hasRows = classRow != null || evaluationRows.isNotEmpty()

	return SummaryWeekItem(
		classRow = classRow,
		evaluations = evaluationRows,
		hasSchedule = hasSchedule,
		card = when {
			!hasSyncedEvaluations -> if (hasRows) null else evaluationsPendingCard()
			!hasEvaluations -> addEvaluationCard()
			!hasRows -> nothingPendingCard(next = nextEvaluation)
			else -> null
		}
	)
}

// The class carries its hour: it is the only row of the week that is about the clock.
private fun SummaryClassMoment.toSummaryClassRowItem(): SummaryClassRowItem {
	return SummaryClassRowItem(
		leadText = if (isInProgress) {
			UiText.Resource(Res.string.summary_class_in_progress)
		} else {
			date.toDayLeadText(isToday = isToday)
		},
		isInProgress = isInProgress,
		subjectCode = meeting.subjectCode,
		place = classPlaceItem(
			startBlock = meeting.startBlock,
			endBlock = meeting.endBlock,
			classroom = meeting.classroom,
			clockRange = ScheduleBlockClock.clockRangeOrNull(
				startBlock = meeting.startBlock,
				endBlock = meeting.endBlock
			)
		)
	)
}

// An evaluation says where, not when: its hour is the class's and the evaluation tab owns it. Its row reads as the one
// of the Evaluations tab: the icon of its type, its name in capitals and its state in words.
private fun SummaryWeekEvaluation.toSummaryEvaluationRowItem(): SummaryEvaluationRowItem {
	return SummaryEvaluationRowItem(
		evaluationId = evaluationId,
		leadText = date.toDayLeadText(isToday = isToday),
		typeIcon = type.toIcon(),
		titleText = UiText.Uppercase(
			UiText.Resource(
				Res.string.summary_evaluation_name,
				listOf(UiText.Resource(type.toLabelResource()), ordinal)
			)
		),
		statusText = UiText.Resource(state.toStatusLabelResource()),
		isOverdue = state == EvaluationState.OVERDUE,
		subjectCode = subjectCode,
		place = meeting?.let { inherited ->
			classPlaceItem(
				startBlock = inherited.startBlock,
				endBlock = inherited.endBlock,
				classroom = inherited.classroom,
				clockRange = null
			)
		},
		weekStart = AcademicWeekEngine.mondayOf(date)
	)
}

// "Hoy" for today and the short day with its number ("Lun 19") for any other.
private fun LocalDate.toDayLeadText(isToday: Boolean): UiText {
	return if (isToday) {
		UiText.Resource(Res.string.summary_day_today)
	} else {
		UiText.Resource(
			Res.string.summary_day_date,
			listOf(UiText.Capitalized(dayOfWeek.toShortNameText()), day)
		)
	}
}

// The name an evaluation has on the hub and in "Próx.": its type, then its number among those of its subject.
internal fun EvaluationType.toLabelResource(): StringResource = when (this) {
	EvaluationType.TEST -> Res.string.summary_evaluation_type_test
	EvaluationType.ESSAY -> Res.string.summary_evaluation_type_essay
	EvaluationType.ATTENDANCE -> Res.string.summary_evaluation_type_attendance
	EvaluationType.INTERVENTIONS -> Res.string.summary_evaluation_type_interventions
	EvaluationType.LABORATORY -> Res.string.summary_evaluation_type_laboratory
	EvaluationType.MODEL -> Res.string.summary_evaluation_type_model
	EvaluationType.PRESENTATION -> Res.string.summary_evaluation_type_presentation
	EvaluationType.PROJECT -> Res.string.summary_evaluation_type_project
	EvaluationType.QUIZ -> Res.string.summary_evaluation_type_quiz
	EvaluationType.REPORT -> Res.string.summary_evaluation_type_report
	EvaluationType.WORKSHOP -> Res.string.summary_evaluation_type_workshop
	EvaluationType.WRITTEN_WORK -> Res.string.summary_evaluation_type_written_work
	EvaluationType.OTHER -> Res.string.summary_evaluation_type_other
}

// The icon of each type is the one the Evaluations tab gives it; a test in scenarios keeps the two lists equal.
fun EvaluationType.toIcon(): ImageVector = when (this) {
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

// The state an evaluation has in the Evaluations tab, in the same words.
internal fun EvaluationState.toStatusLabelResource(): StringResource = when (this) {
	EvaluationState.PENDING -> Res.string.summary_evaluation_status_scheduled
	EvaluationState.OVERDUE -> Res.string.summary_evaluation_status_overdue
	EvaluationState.COMPLETED -> Res.string.summary_evaluation_status_completed
	EvaluationState.CONTINUOUS -> Res.string.summary_evaluation_status_continuous
}
