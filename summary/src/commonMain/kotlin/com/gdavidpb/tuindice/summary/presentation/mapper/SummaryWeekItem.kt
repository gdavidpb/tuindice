package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleBlockClock
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
	return SummaryWeekItem(
		classRow = classMoment?.toSummaryClassRowItem(),
		evaluations = evaluations.map { evaluation -> evaluation.toSummaryEvaluationRowItem() }
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

// An evaluation says where, not when: its hour is the class's and the evaluation tab owns it.
private fun SummaryWeekEvaluation.toSummaryEvaluationRowItem(): SummaryEvaluationRowItem {
	return SummaryEvaluationRowItem(
		evaluationId = evaluationId,
		leadText = date.toDayLeadText(isToday = isToday),
		titleText = UiText.Resource(
			Res.string.summary_evaluation_name,
			listOf(UiText.Resource(type.toLabelResource()), ordinal)
		),
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

private fun EvaluationType.toLabelResource(): StringResource = when (this) {
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
