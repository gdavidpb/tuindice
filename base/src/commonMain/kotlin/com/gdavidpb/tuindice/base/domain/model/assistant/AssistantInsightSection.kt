package com.gdavidpb.tuindice.base.domain.model.assistant

/** One section of a term report: its [title] and its [text], as the assistant wrote them. */
data class AssistantInsightSection(
	val title: String,
	val text: String
)
