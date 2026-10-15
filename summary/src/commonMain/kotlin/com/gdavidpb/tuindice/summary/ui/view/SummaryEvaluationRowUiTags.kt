package com.gdavidpb.tuindice.summary.ui.view

/**
 * The tags of the parts of an evaluation row of the week. They live apart from `SummaryUiTags`, which already holds as
 * many functions as its detekt threshold allows.
 */
object SummaryEvaluationRowUiTags {
	/** The icon of the type of the evaluation, the first thing its row draws. */
	fun typeIcon(evaluationId: String): String = "summary_week_evaluation_type_icon_$evaluationId"

	/** The day that leads the row, at the width the pill of the class row has. */
	fun lead(evaluationId: String): String = "summary_week_evaluation_lead_$evaluationId"
}
