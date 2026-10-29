package com.gdavidpb.tuindice.assistant.ui.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The sizes of the actions under an agent message (copy, the thumbs, report). The buttons are drawn at [Button], side
// by side with no space between them, so the row stays smaller than the message. Each still takes touches 48 dp wide:
// the hit test of Compose widens a touch target smaller than the minimum of the platform, and between two overlapping
// targets the nearest one wins.
object AssistantMessageActionSizes {
	val Button: Dp = 36.dp
	val Icon: Dp = 18.dp
}
