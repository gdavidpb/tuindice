package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason

data class ReportAssistantMessageParams(
	val messageId: String,
	val reason: ReportReason,
	val comment: String?
)
