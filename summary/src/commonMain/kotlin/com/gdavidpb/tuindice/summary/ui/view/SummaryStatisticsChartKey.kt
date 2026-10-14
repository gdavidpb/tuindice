package com.gdavidpb.tuindice.summary.ui.view

/**
 * The keys of the Statistics charts. Each names the tags of its chart and of the points it draws, so a test finds the
 * chart it means and no two charts share a tag.
 */
internal object SummaryStatisticsChartKey {
	const val CurrentTerm = "current_term"
	const val Index = "index"
	const val Subjects = "subjects"
	const val Credits = "credits"
	const val Grades = "grades"
	const val Pace = "pace"
}
