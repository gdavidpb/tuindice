package com.gdavidpb.tuindice.record.presentation.model

// One meeting of a subject placed on a day column. Blocks are 1-based and inclusive; the lane
// splits the width of the cell with the meetings that overlap it on the same day.
data class ScheduleCellItem(
	val attemptId: String,
	val codeText: String,
	val classroomText: String?,
	val startBlock: Int,
	val endBlock: Int,
	val lane: Int,
	val laneCount: Int,
	// What the university flagged on the enrollment of the subject, as the chip on its row says it.
	val errorText: String? = null
) {
	val blockSpan: Int
		get() = endBlock - startBlock + 1

	val hasError: Boolean
		get() = errorText != null

	// Overlaps another subject's meeting that day: both views mark it with the alert tone.
	val isClash: Boolean
		get() = laneCount > 1
}
