package com.gdavidpb.tuindice.record.presentation.model

// Where a notice lives. A provisional annulment and stale data belong to the current term, so they
// sit on its page; a final annulment has no current term to sit on and goes above the pager.
enum class RecordNoticeKind {
	AnnulledProvisional,
	AnnulledFinal,
	StaleEnrollment
}
