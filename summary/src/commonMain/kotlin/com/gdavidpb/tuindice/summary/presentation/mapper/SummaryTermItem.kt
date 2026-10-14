package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_points_rest
import tuindice.summary.generated.resources.summary_term_week_description
import tuindice.summary.generated.resources.summary_term_week_rest
import tuindice.summary.generated.resources.summary_term_week_value

/**
 * The term as the screen shows it, or null when it would have nothing under its name: no week to
 * say (outside weeks 1 to 12) and no subject whose points are known.
 */
fun SummaryTerm.toSummaryTermItemOrNull(): SummaryTermItem? {
	if (academicWeek == null && subjects.isEmpty()) return null

	return SummaryTermItem(
		nameText = UiText.Raw("${periodCode.shortLabel} $periodYear"),
		week = academicWeek?.toSummaryTermWeekItem(),
		subjects = subjects.map { subject -> subject.toSummarySubjectPointsItem() }
	)
}

private fun Int.toSummaryTermWeekItem(): SummaryTermWeekItem {
	val totalWeeks = AcademicWeekEngine.MAX_ACADEMIC_WEEK

	return SummaryTermWeekItem(
		week = this,
		totalWeeks = totalWeeks,
		valueText = UiText.Resource(Res.string.summary_term_week_value, listOf(this)),
		restText = UiText.Resource(Res.string.summary_term_week_rest, listOf(totalWeeks)),
		description = UiText.Resource(Res.string.summary_term_week_description, listOf(this, totalWeeks))
	)
}

private fun SummarySubjectPoints.toSummarySubjectPointsItem(): SummarySubjectPointsItem {
	return if (points == null) {
		SummarySubjectPointsItem(
			subjectCode = subjectCode,
			valueText = null,
			restText = UiText.Resource(Res.string.summary_points_none),
			attemptId = attemptId
		)
	} else {
		SummarySubjectPointsItem(
			subjectCode = subjectCode,
			valueText = points.earned.formatCompactPoints(),
			restText = UiText.Resource(Res.string.summary_points_rest, listOf(points.evaluated.formatCompactPoints())),
			attemptId = attemptId
		)
	}
}

// 41 and not 41.00, the way the Evaluations tab writes a grade that has no decimals.
internal fun Double.formatCompactPoints(): String {
	return if (this == toInt().toDouble()) formatGrade(decimals = 0) else formatGrade(decimals = 2)
}
