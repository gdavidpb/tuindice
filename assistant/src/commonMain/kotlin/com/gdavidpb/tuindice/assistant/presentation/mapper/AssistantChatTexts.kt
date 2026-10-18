package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode

// The texts of the chat as ready strings. The machine reads them through this port, so its rows can run on the host,
// where the compose resources cannot be read. The app binds the resource-backed implementation.
interface AssistantChatTexts {
	suspend fun screenName(screen: AssistantScreen): String

	suspend fun evaluationTypeName(evaluationType: String): String

	suspend fun maxGrade(maxGrade: Double): String

	suspend fun undated(): String

	suspend fun guided(): String

	suspend fun quotaExhausted(retryAfterSeconds: Int?): String

	suspend fun restricted(level: StandingLevel?): String

	// The text of a failed turn. An OTHER failure that cannot be retried does not invite a retry.
	suspend fun turnFailure(code: RunErrorCode, retryable: Boolean): String

	// The text of a turn whose result is unknown: the student can send the message again.
	suspend fun turnUnconfirmed(): String

	suspend fun sendFailed(): String

	suspend fun messageTooLong(maxLength: Int): String
}
