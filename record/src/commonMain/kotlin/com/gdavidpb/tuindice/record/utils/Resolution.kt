package com.gdavidpb.tuindice.record.utils

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.record.data.model.QuarterSyncResolution
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter

fun resolveQuarterSyncResolution(
	incomingQuarters: List<LocalQuarter>,
	pendingMutations: List<PendingMutation<RecordMutation>>
): QuarterSyncResolution {
	val replacedClosedQuarterIds = incomingQuarters
		.asSequence()
		.filter { quarter -> quarter.isReadOnly }
		.mapTo(linkedSetOf()) { quarter -> quarter.id }

	if (replacedClosedQuarterIds.isEmpty()) {
		return QuarterSyncResolution(
			replacedClosedQuarterIds = emptySet(),
			invalidatedMutationIds = emptySet(),
			compatiblePendingMutations = pendingMutations
		)
	}

	val invalidatedMutationIds = pendingMutations
		.mapNotNullTo(linkedSetOf()) { mutation ->
			when (val payload = mutation.mutation) {
				is RecordMutation.SetSubjectGrade ->
					mutation.mutationId.takeIf { payload.quarterId in replacedClosedQuarterIds }

				is RecordMutation.RemoveQuarter ->
					mutation.mutationId.takeIf { payload.quarterId in replacedClosedQuarterIds }
			}
		}

	return QuarterSyncResolution(
		replacedClosedQuarterIds = replacedClosedQuarterIds,
		invalidatedMutationIds = invalidatedMutationIds,
		compatiblePendingMutations = pendingMutations.filterNot { mutation ->
			mutation.mutationId in invalidatedMutationIds
		}
	)
}