package com.gdavidpb.tuindice.record.data.model

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation

data class QuarterSyncResolution(
	val replacedClosedQuarterIds: Set<String>,
	val invalidatedQuarterIds: Set<String>,
	val invalidatedMutationIds: Set<String>,
	val compatiblePendingMutations: List<PendingMutation<RecordMutation>>
)
