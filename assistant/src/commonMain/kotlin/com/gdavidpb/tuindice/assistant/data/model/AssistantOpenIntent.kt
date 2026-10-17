package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.chatcontract.intent.Intent

/** An intent the assistant proposed that the user has not resolved yet, with the message that carries it. */
data class AssistantOpenIntent(
	val intentId: String,
	val turnId: String,
	val messageId: String,
	val intent: Intent
)
