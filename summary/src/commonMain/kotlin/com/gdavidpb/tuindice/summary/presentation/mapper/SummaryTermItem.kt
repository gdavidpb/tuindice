package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SUBJECT_POINTS_TOTAL
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_day_date
import tuindice.summary.generated.resources.summary_evaluation_name
import tuindice.summary.generated.resources.summary_link_record
import tuindice.summary.generated.resources.summary_points_evaluated
import tuindice.summary.generated.resources.summary_points_no_evaluations
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_points_rest
import tuindice.summary.generated.resources.summary_term_next_evaluation
import tuindice.summary.generated.resources.summary_term_next_today
import tuindice.summary.generated.resources.summary_term_subjects_many
import tuindice.summary.generated.resources.summary_term_subjects_one
import tuindice.summary.generated.resources.summary_term_week_description
import tuindice.summary.generated.resources.summary_term_week_last
import tuindice.summary.generated.resources.summary_term_week_remaining_many
import tuindice.summary.generated.resources.summary_term_week_remaining_one
import tuindice.summary.generated.resources.summary_term_week_rest
import tuindice.summary.generated.resources.summary_term_week_value

/**
 * The term as the screen shows it. A term in progress with nothing to show (no week and no subject whose points are
 * known) says what is still to come in its card; with none in progress, the link leads to Registro, the last finished
 * term comes first if there is one, and then the invitation to plan the next one (or the first one, for a new student).
 */
fun SummaryTerm.toSummaryTermItem(): SummaryTermItem {
	return when (this) {
		is SummaryTerm.InProgress -> toInProgressItem()

		is SummaryTerm.Idle -> SummaryTermItem(
			linkText = if (hasTerms) UiText.Resource(Res.string.summary_link_record) else null,
			week = null,
			summaryText = null,
			subjects = emptyList(),
			finished = finished?.toSummaryFinishedTermItem(),
			card = if (hasTerms) planTermCard() else firstTermCard()
		)
	}
}

private fun SummaryTerm.InProgress.toInProgressItem(): SummaryTermItem {
	val week = toSummaryTermWeekItemOrNull()
	val subjectItems = subjects.map { subject -> subject.toSummarySubjectPointsItem() }
	val hasContent = week != null || subjectItems.isNotEmpty()

	return SummaryTermItem(
		linkText = UiText.Raw("${periodCode.shortLabel} $periodYear"),
		week = week,
		summaryText = toSummaryTextOrNull(),
		subjects = subjectItems,
		finished = null,
		card = if (hasContent) null else evaluationsPendingCard()
	)
}

// The week and what is left of the term come together or not at all: both are counted from the week it is in.
private fun SummaryTerm.InProgress.toSummaryTermWeekItemOrNull(): SummaryTermWeekItem? {
	val week = academicWeek
	val weeksLeft = weeksLeft

	return if (week == null || weeksLeft == null) {
		null
	} else {
		val totalWeeks = AcademicWeekEngine.MAX_ACADEMIC_WEEK

		SummaryTermWeekItem(
			week = week,
			totalWeeks = totalWeeks,
			valueText = UiText.Resource(Res.string.summary_term_week_value, listOf(week)),
			restText = UiText.Resource(Res.string.summary_term_week_rest, listOf(totalWeeks)),
			description = UiText.Resource(Res.string.summary_term_week_description, listOf(week, totalWeeks)),
			remainingText = weeksLeft.toRemainingText()
		)
	}
}

// "faltan 9 semanas", "falta 1 semana" and, in the twelfth, "última semana".
private fun Int.toRemainingText(): UiText {
	return when (this) {
		0 -> UiText.Resource(Res.string.summary_term_week_last)
		1 -> UiText.Resource(Res.string.summary_term_week_remaining_one)
		else -> UiText.Resource(Res.string.summary_term_week_remaining_many, listOf(this))
	}
}

// "4 materias · 16 UC": how many subjects the rows are, and the credits they add up to. Absent with no rows.
private fun SummaryTerm.InProgress.toSummaryTextOrNull(): UiText? {
	val count = subjects.size
	if (count == 0) return null

	return if (count == 1) {
		UiText.Resource(Res.string.summary_term_subjects_one, listOf(count, credits))
	} else {
		UiText.Resource(Res.string.summary_term_subjects_many, listOf(count, credits))
	}
}

private fun SummarySubjectPoints.toSummarySubjectPointsItem(): SummarySubjectPointsItem {
	val graded = points?.takeIf { attemptPoints -> attemptPoints.gradedCount > 0 }

	return SummarySubjectPointsItem(
		subjectCode = subjectCode,
		valueText = graded?.earned?.formatCompactPoints(),
		restText = when {
			graded != null -> UiText.Resource(
				Res.string.summary_points_rest,
				listOf(graded.evaluated.formatCompactPoints())
			)

			points != null -> UiText.Resource(Res.string.summary_points_none)
			else -> UiText.Resource(Res.string.summary_points_no_evaluations)
		},
		attemptId = attemptId,
		evaluatedText = UiText.Resource(
			Res.string.summary_points_evaluated,
			listOf(evaluatedOfTotal.formatCompactPoints(), SUBJECT_POINTS_TOTAL.formatCompactPoints())
		),
		evaluatedFraction = (evaluatedOfTotal / SUBJECT_POINTS_TOTAL).toFloat(),
		nextText = nextEvaluation?.toNextText()
	)
}

// "Próx.: Parcial 2 · lun 26": the name of the evaluation, then its day the way the hub writes a day ("hoy" today).
private fun SummaryNextEvaluation.toNextText(): UiText {
	return UiText.Resource(Res.string.summary_term_next_evaluation, listOf(toNameText(), toDayText()))
}

// The name an evaluation has on the hub: its type and its number among those of its subject ("Parcial 2").
internal fun SummaryNextEvaluation.toNameText(): UiText {
	return UiText.Resource(
		Res.string.summary_evaluation_name,
		listOf(UiText.Resource(type.toLabelResource()), ordinal)
	)
}

// The day of an evaluation the way the hub writes a day in a sentence: "hoy" for today, "lun 26" for any other.
internal fun SummaryNextEvaluation.toDayText(): UiText {
	return if (isToday) {
		UiText.Resource(Res.string.summary_term_next_today)
	} else {
		UiText.Resource(Res.string.summary_day_date, listOf(date.dayOfWeek.toShortNameText(), date.day))
	}
}

// 41 and not 41.00, the way the Evaluations tab writes a grade that has no decimals.
internal fun Double.formatCompactPoints(): String {
	return if (this == toInt().toDouble()) formatGrade(decimals = 0) else formatGrade(decimals = 2)
}
