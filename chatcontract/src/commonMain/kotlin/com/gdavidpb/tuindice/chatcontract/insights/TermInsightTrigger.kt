package com.gdavidpb.tuindice.chatcontract.insights

/** The wire values of [TermInsight.kind]: what asked for the report. The term start has no report. */
public object TermInsightTrigger {
	/** The report of a term at its end, written in its last academic week. */
	public const val TERM_END: String = "term_end"

	/** The report of a term when new grades reach it. */
	public const val NEW_GRADES: String = "new_grades"
}
