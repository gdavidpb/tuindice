package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.presentation.model.UiText

// One meeting of a subject placed on a day column, with everything it shows already resolved.
// Blocks are 1-based and inclusive; the lane splits the width of the cell with the meetings that
// overlap it on the same day.
data class ScheduleCellItem(
	val attemptId: String,
	val codeText: String,
	// The room, only for a meeting tall enough to have a second line to show it on.
	val classroomText: String?,
	// The range of blocks the table prints: "1-2", or "3" for a single block.
	val blocksText: String,
	// What a screen reader says of the cell on the week grid.
	val description: UiText,
	// The colours of the subject's chip, so a subject looks the same here as everywhere else.
	val codeColor: Color,
	val codeContainerColor: Color,
	val startBlock: Int,
	val endBlock: Int,
	val lane: Int,
	val laneCount: Int,
	// Today's meeting whose blocks hold the current one: the class being taught right now.
	val isInProgress: Boolean = false
) {
	val blockSpan: Int
		get() = endBlock - startBlock + 1

	// Overlaps another subject's meeting that day: both views mark it with the alert tone.
	val isClash: Boolean
		get() = laneCount > 1
}
