package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate

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
	// What a screen reader says of the cell on the week grid, and of a marked cell of the table, which
	// is a button on its own; the rest of the table is said by its row.
	val description: UiText,
	// The colours of the subject's chip, so a subject looks the same here as everywhere else.
	val codeColor: Color,
	val codeContainerColor: Color,
	val startBlock: Int,
	val endBlock: Int,
	val lane: Int,
	val laneCount: Int,
	// Today's meeting whose blocks hold the current one: the class being taught right now.
	val isInProgress: Boolean = false,
	// The Monday that opens the calendar week of an evaluation that falls on this meeting, or null
	// when none does. Both views mark the cell, and a tap on it asks the Evaluations tab for the week the content has for it.
	val evaluationWeekStart: LocalDate? = null,
	// The id of the evaluation that marks this meeting, the earliest one on it (see ScheduleEvaluationMarks),
	// or null when none falls on it. A tap asks the Evaluations tab for it, so the tab opens on that evaluation.
	val evaluationId: String? = null
) {
	val blockSpan: Int
		get() = endBlock - startBlock + 1

	// Overlaps another subject's meeting that day: both views mark it with the alert tone.
	val isClash: Boolean
		get() = laneCount > 1

	// An evaluation of this calendar week falls on this meeting (one mark however many there are).
	val hasEvaluation: Boolean
		get() = evaluationWeekStart != null
}
