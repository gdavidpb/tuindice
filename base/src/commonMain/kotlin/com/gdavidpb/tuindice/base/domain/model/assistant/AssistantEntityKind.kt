package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlinx.serialization.Serializable

@Serializable
enum class AssistantEntityKind {
	TERM,
	SUBJECT,
	EVALUATION,
	ATTEMPT,
	PENSUM,
	PLAN
}
