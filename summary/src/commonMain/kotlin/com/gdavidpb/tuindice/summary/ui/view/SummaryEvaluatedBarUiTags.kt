package com.gdavidpb.tuindice.summary.ui.view

/**
 * The tags of the evaluated bar of a subject's row. They live apart from `SummaryUiTags`, which already holds as many
 * functions as its detekt threshold allows. The bar itself is `SummaryUiTags.termPointsBar`, and its part evaluated but
 * not earned yet is `SummaryUiTags.termPointsBarFill`; the earned part, drawn first from the start, is named here.
 */
object SummaryEvaluatedBarUiTags {
	/** The earned part of the bar: the share of its points the subject has earned, at its full colour. */
	fun earned(subjectCode: String): String = "summary_term_points_bar_earned_$subjectCode"
}
