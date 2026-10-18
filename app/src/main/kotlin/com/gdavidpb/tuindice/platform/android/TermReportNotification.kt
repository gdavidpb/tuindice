package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.domain.model.TermReportPush

// The insight of the term report a push asks to show, or null when the push is not one about a term report, or does not
// name the report. The keys are the ones the backend sends, shared with the tap that opens the report.
internal fun termReportInsightIdOf(data: Map<String, String>): String? {
	val isTermReport = data[TermReportPush.TYPE_KEY] == TermReportPush.TERM_REPORT_TYPE

	return if (isTermReport) data[TermReportPush.INSIGHT_ID_KEY]?.takeIf(String::isNotEmpty) else null
}

// The extras of the intent a tap on the notification opens: the same keys the push carries, which MainActivity reads.
internal fun termReportExtrasOf(insightId: String): Map<String, String> {
	return mapOf(
		TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE,
		TermReportPush.INSIGHT_ID_KEY to insightId
	)
}

// The notification is shown only when the student allows notifications: the permission to post them where the system
// asks for it, and the notifications of the app switched on.
internal fun canShowTermReportNotification(
	isPermissionGranted: Boolean,
	areNotificationsEnabled: Boolean
): Boolean = isPermissionGranted && areNotificationsEnabled
