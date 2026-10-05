package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.base.domain.model.User

// What Summary reads locally in one observation: the user and whether the local record still has a
// current term, which is what tells a provisional annulment (the term is kept) from a final one.
data class ObservedSummary(
	val user: User,
	val hasCurrentTerm: Boolean
)
