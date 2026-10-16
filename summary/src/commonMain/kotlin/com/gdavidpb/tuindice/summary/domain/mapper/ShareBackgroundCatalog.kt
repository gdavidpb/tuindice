package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground

// The backgrounds a card can wear: the one the app ships with first, then the catalog in the order it came in, without
// the app's own background a second time. An empty catalog leaves only the app's background.
internal fun shareBackgroundsOf(catalog: List<ShareBackground>): List<ShareBackground> {
	return listOf(ShareBackground.included) + catalog.filter { background ->
		background.id != ShareBackground.INCLUDED_ID
	}
}
