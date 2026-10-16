package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground

// The background a card opens with: the one the student chose last when it is still in the list, and the first one
// otherwise, so that a background that left the catalog never leaves the picker empty.
internal fun List<ShareBackground>.selectShareBackground(preferredId: String?): ShareBackground? {
	return find { background -> background.id == preferredId } ?: firstOrNull()
}
