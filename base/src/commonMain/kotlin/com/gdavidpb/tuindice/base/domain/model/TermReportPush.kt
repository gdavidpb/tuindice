package com.gdavidpb.tuindice.base.domain.model

/**
 * The data a push about a term report carries, as the backend sends it: every value is a string. A tap on that push
 * opens the report of [INSIGHT_ID_KEY] when [TYPE_KEY] names [TERM_REPORT_TYPE]; the platforms read the keys from the
 * push's data.
 */
object TermReportPush {
	const val TYPE_KEY = "type"
	const val TERM_REPORT_TYPE = "term_report"
	const val INSIGHT_ID_KEY = "insightId"
}
