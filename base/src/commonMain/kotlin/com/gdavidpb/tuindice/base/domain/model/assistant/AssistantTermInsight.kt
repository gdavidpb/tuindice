package com.gdavidpb.tuindice.base.domain.model.assistant

/**
 * A term report the assistant wrote about one academic term. [id] is the insight id, `<termId>:<sequence>`, and [termKey]
 * the term it is about. [kind] is the wire trigger that asked for it, `term_end` or `new_grades`. [sections] are what the
 * report says, in order; [nextActions] are the buttons of "Qué sigue". [createdAtMillis] is the instant the service wrote
 * the report, in epoch milliseconds.
 */
data class AssistantTermInsight(
	val id: String,
	val termKey: String,
	val kind: String,
	val title: String,
	val body: String,
	val entityRefs: List<AssistantEntityRef>,
	val sections: List<AssistantInsightSection> = emptyList(),
	val nextActions: List<AssistantInsightAction> = emptyList(),
	val createdAtMillis: Long = 0L
)
