package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

/** The conversation as the screen reads it: its items in order, the intents still open, and the turn last seen. */
data class AssistantConversation(
	val conversationId: String?,
	val items: List<AssistantConversationItem>,
	val openIntents: Map<String, Intent>,
	val resolvedIntents: Map<String, OutcomeKind>,
	// The detail codes of the intents the app resolved, by intent id: a refusal keeps the reason it was given.
	val resolvedDetails: Map<String, String> = emptyMap(),
	// The rows applied and marked of the resolved intents that add evaluations: the receipt says what was added.
	val resolvedCounts: Map<String, IntentResolvedCount> = emptyMap(),
	// The intents resolved with an outcome this build does not know: their cards close with no outcome label.
	val unknownOutcomeIntents: Set<String> = emptySet(),
	val activeTurn: AssistantActiveTurn?,
	// The session the conversation belongs to. A retry decided on the sheet carries it to the write that replaces the
	// question it answers, so a write made for a session that has ended is refused by the store.
	val sessionMark: Int = 0
)
