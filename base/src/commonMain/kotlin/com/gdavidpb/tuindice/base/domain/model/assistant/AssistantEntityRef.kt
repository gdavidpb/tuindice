package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlinx.serialization.Serializable

@Serializable
data class AssistantEntityRef(
	val kind: AssistantEntityKind,
	val id: String
)
