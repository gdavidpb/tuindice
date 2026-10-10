package com.gdavidpb.tuindice.record.presentation.model

// What a notice is about. A provisional annulment and stale data belong to the current term; a final
// annulment has dropped it. Where each one sits and its icon follow from here, in the resolver.
enum class RecordNoticeKind {
	AnnulledProvisional,
	AnnulledFinal,
	StaleEnrollment
}
