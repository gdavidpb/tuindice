package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason

internal fun AssistantReportReason.toWire(): ReportReason {
	return when (this) {
		AssistantReportReason.INCORRECT -> ReportReason.INCORRECT
		AssistantReportReason.INAPPROPRIATE -> ReportReason.INAPPROPRIATE
		AssistantReportReason.HARMFUL -> ReportReason.HARMFUL
		AssistantReportReason.OTHER -> ReportReason.OTHER
	}
}
