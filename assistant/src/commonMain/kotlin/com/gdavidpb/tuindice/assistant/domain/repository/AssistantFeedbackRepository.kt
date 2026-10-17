package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport

interface AssistantFeedbackRepository {
	suspend fun rate(feedback: MessageFeedback)

	suspend fun report(report: MessageReport)
}
