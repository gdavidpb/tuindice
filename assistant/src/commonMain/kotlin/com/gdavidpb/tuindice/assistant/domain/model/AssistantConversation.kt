package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

/** The conversation as the screen reads it: its items in order, the intents still open, and the turn last seen. */
data class AssistantConversation(
	val conversationId: String?,
	val items: List<AssistantConversationItem>,
	val openIntents: Map<String, Intent>,
	val resolvedIntents: Map<String, OutcomeKind>,
	// The rows applied and marked of the resolved intents that add evaluations: the receipt says what was added.
	val resolvedCounts: Map<String, IntentResolvedCount> = emptyMap(),
	val activeTurn: AssistantActiveTurn?
)
