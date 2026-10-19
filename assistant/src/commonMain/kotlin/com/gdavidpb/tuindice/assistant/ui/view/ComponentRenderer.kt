package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AcademicComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumPlanComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.SistemaComponentItem

// Draws one component of a message by its family. Each family draws its own items in its own renderer, so each `when`
// stays exhaustive over its family and small.
@Composable
internal fun ComponentRenderer(
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is SistemaComponentItem -> SistemaComponentRenderer(item = item, onAction = onAction)
		is RecordComponentItem -> RecordComponentRenderer(item = item, onAction = onAction)
		is AcademicComponentItem -> AcademicComponentRenderer(item = item, onAction = onAction)
		is PensumPlanComponentItem -> PensumPlanComponentRenderer(item = item, onAction = onAction)
		is ChatFallbackItem -> ChatFallbackComponentView(fallbackText = item.fallbackText)
	}
}
