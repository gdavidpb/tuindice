package com.gdavidpb.tuindice.ui.activity

import com.gdavidpb.tuindice.base.domain.model.TermReportPush

// Whether the intent the activity is created with carries a term report push to open. A recreated activity has read its
// intent when it was first created, and an intent the system brings back from history was delivered before: neither
// opens the report again.
internal fun shouldOpenTermReportOnCreate(
	isRestored: Boolean,
	isLaunchedFromHistory: Boolean
): Boolean = !isRestored && !isLaunchedFromHistory

// The insight a term report push opens: the one its data names, when the push is a term report and names one. Null for
// any other push, which opens no report.
internal fun termReportInsightIdOrNull(type: String?, insightId: String?): String? {
	return insightId.takeIf { type == TermReportPush.TERM_REPORT_TYPE }
}
