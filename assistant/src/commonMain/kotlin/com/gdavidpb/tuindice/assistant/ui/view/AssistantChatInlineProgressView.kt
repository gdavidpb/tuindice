package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull

// A turn's status belongs with its student's message, rather than beside the composer. The label comes presented
// (AssistantChatPresentation): the view only paints it, and keeps the last label while the status leaves.
@Composable
fun AssistantChatInlineProgressView(label: String?) {
	val shownLabel = rememberLastNonNull(label)

	AssistantChatSlotView(visible = label != null) {
		shownLabel?.let { AssistantProgressView(label = it) }
	}
}
