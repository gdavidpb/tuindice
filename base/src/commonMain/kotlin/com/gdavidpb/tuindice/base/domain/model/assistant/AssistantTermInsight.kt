package com.gdavidpb.tuindice.base.domain.model.assistant

data class AssistantTermInsight(
	val id: String,
	val termKey: String,
	val kind: String,
	val title: String,
	val body: String,
	val entityRefs: List<AssistantEntityRef>
)
