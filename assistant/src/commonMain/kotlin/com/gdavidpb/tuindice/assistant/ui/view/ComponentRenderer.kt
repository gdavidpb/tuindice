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
// stays exhaustive over its family and small. A [rowLimit] cuts the rows of a list, and null draws them all.
@Composable
internal fun ComponentRenderer(
	item: ChatComponentItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is SistemaComponentItem -> SistemaComponentRenderer(item = item, onAction = onAction)
		is RecordComponentItem -> RecordComponentRenderer(item = item, rowLimit = rowLimit, onAction = onAction)
		is AcademicComponentItem -> AcademicComponentRenderer(item = item, rowLimit = rowLimit, onAction = onAction)
		is PensumPlanComponentItem -> PensumPlanComponentRenderer(item = item, rowLimit = rowLimit, onAction = onAction)
		is ChatFallbackItem -> ChatFallbackComponentView(fallbackText = item.fallbackText)
	}
}
