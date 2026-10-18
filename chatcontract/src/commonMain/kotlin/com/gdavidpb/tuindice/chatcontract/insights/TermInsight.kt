package com.gdavidpb.tuindice.chatcontract.insights

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/**
 * The term report of one academic term, identified by [termKey]. [id] is the insight id, `<termId>:<sequence>`: the
 * path segment of the feedback endpoint (`POST /assistant/v1/insights/{insightId}/feedback`).
 *
 * [kind] is the trigger that asked for the report, one of the [TermInsightTrigger] values: [TermInsightTrigger.TERM_END]
 * or [TermInsightTrigger.NEW_GRADES]. It is the wire trigger itself, so no other field carries it.
 *
 * [title] and [body] stay populated for the clients that show plain text: the title is the first section, and the body
 * is the text of the first section followed by the other sections. [sections] has the structure a screen needs. [nextActions]
 * are the buttons of "Qué sigue", at most three, chosen by fixed rules on the service and never written by the model.
 * [createdAtMillis] is the instant the service created the report, in epoch milliseconds.
 */
@Serializable
public data class TermInsight(
	public val id: String,
	public val termKey: String,
	public val kind: String,
	public val title: String,
	public val body: String,
	public val entityRefs: List<EntityRef> = emptyList(),
	public val sections: List<InsightSection> = emptyList(),
	public val nextActions: List<InsightAction> = emptyList(),
	public val createdAtMillis: Long = 0L
)
