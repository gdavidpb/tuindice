package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget

/**
 * A button of "Qué sigue": its [label], the screen it opens ([target]), the mark of that screen ([icon]) and the
 * records that screen needs.
 */
@Immutable
data class TermReportActionItem(
	val label: String,
	val target: AssistantNavTarget,
	val icon: TermReportActionIconKind,
	val entityRefs: List<AssistantEntityRef>
)
