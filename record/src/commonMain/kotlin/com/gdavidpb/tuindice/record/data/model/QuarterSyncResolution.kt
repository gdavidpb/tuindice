package com.gdavidpb.tuindice.record.data.model

import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation

data class QuarterSyncResolution(
	val replacedClosedQuarterIds: Set<String>,
	val invalidatedQuarterIds: Set<String>,
	val invalidatedMutationIds: Set<String>,
	val compatiblePendingMutations: List<MutationEnvelope<String, RecordMutation>>
)
