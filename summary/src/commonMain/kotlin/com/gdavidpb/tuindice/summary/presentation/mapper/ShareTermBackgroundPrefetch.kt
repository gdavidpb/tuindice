package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import kotlin.math.abs

// The card pictures to bring ahead of time: those of the remote backgrounds the student has not chosen, nearest to the
// chosen one first, so the ones a swipe reaches come before the far ones. The chosen one is loaded by the card itself,
// and the one the app ships with needs no loading.
internal fun List<ShareTermBackgroundItem>.cardUrlsToPrefetch(): List<String> {
	val chosenIndex = indexOfFirst { item -> item.selected }.coerceAtLeast(0)

	return withIndex()
		.filterNot { (_, item) -> item.selected }
		.sortedBy { (index, _) -> abs(index - chosenIndex) }
		.mapNotNull { (_, item) -> (item.artwork.art as? ShareTermArt.Remote)?.cardUrl }
}
