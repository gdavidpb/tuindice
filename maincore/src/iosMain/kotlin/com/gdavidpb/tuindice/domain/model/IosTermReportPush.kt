package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.base.domain.model.TermReportPush

// The keys of a term report push, as the iOS host reads them from the push's data. The values are the ones of
// TermReportPush: the framework only exposes what maincore declares, so the host reads them from here.
object IosTermReportPush {
	val typeKey: String = TermReportPush.TYPE_KEY
	val termReportType: String = TermReportPush.TERM_REPORT_TYPE
	val insightIdKey: String = TermReportPush.INSIGHT_ID_KEY
}
