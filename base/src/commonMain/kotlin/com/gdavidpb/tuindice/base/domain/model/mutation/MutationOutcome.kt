package com.gdavidpb.tuindice.base.domain.model.mutation

// The terminal result of one outbox mutation, identified by the id it was submitted with. detailCode is a
// stable code (the failure kind, for instance), never the text of the failure.
data class MutationOutcome(
	val mutationId: String,
	val kind: MutationOutcomeKind,
	val detailCode: String? = null
)
