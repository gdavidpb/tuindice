package com.gdavidpb.tuindice.base.ui.model

// The test tags of a week strip. The strip and the selector of its weeks are the same in every week, while the chip and
// the page of a week are named by the week's key, so the screen that shows the strip names them.
data class WeekStripTags<K>(
	val strip: String,
	val selector: WeekSelectorTags,
	val chip: (K) -> String,
	val page: (K) -> String
)
