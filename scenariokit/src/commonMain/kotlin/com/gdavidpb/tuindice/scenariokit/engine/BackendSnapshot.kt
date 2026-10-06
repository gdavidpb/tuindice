package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.MockState

/** The backend as the failure report sees it: newest requests first, mock scenarios not in `Started`. */
internal data class BackendSnapshot(
	val requests: List<JournalEntry>,
	val unstarted: List<MockState>,
	val error: String?
)
