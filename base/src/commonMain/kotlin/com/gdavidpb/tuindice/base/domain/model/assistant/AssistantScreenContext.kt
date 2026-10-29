package com.gdavidpb.tuindice.base.domain.model.assistant

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class AssistantScreenContext(
	val screen: AssistantScreen,
	val entities: List<AssistantEntityRef> = emptyList(),
	val visibleFields: Map<String, String> = emptyMap(),
	val filters: Map<String, String> = emptyMap()
)
