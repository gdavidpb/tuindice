package com.gdavidpb.tuindice.summary.ui

/**
 * The tags of the term report: the line that offers it in the summary, and the screen with its parts, in the order the
 * screen draws them.
 */
object TermReportUiTags {
	// The line of the summary that offers a term report not opened yet.
	const val TermReportEntry = "summary_term_report_entry"

	const val TermReportContent = "summary_term_report_content"
	const val TermReportHero = "summary_term_report_hero"
	const val TermReportIndex = "summary_term_report_index"
	const val TermReportIndexDelta = "summary_term_report_index_delta"
	const val TermReportCumulativeIndex = "summary_term_report_cumulative_index"
	const val TermReportCumulativeDelta = "summary_term_report_cumulative_delta"
	const val TermReportSubjects = "summary_term_report_subjects"
	const val TermReportCredits = "summary_term_report_credits"
	const val TermReportLegend = "summary_term_report_legend"
	const val TermReportPace = "summary_term_report_pace"
	const val TermReportPaceEmpty = "summary_term_report_pace_empty"
	const val TermReportPaceLegend = "summary_term_report_pace_legend"
	const val TermReportAssistant = "summary_term_report_assistant"
	const val TermReportAskButton = "summary_term_report_ask_button"
	const val TermReportNextActions = "summary_term_report_next_actions"
	const val TermReportNoReport = "summary_term_report_no_report"
	const val TermReportFailed = "summary_term_report_failed"
	const val TermReportRetryButton = "summary_term_report_retry_button"

	// The two thumbs of the report. They are named constants so that the E2E tag checks can see them.
	const val TermReportThumbUseful = "summary_term_report_thumb_useful"
	const val TermReportThumbNotUseful = "summary_term_report_thumb_not_useful"

	fun termReportSubject(subjectCode: String): String = "summary_term_report_subject_$subjectCode"

	fun termReportThumb(useful: Boolean): String = if (useful) TermReportThumbUseful else TermReportThumbNotUseful

	fun termReportSection(index: Int): String = "summary_term_report_section_$index"

	fun termReportNextAction(index: Int): String = "summary_term_report_next_action_$index"
}
