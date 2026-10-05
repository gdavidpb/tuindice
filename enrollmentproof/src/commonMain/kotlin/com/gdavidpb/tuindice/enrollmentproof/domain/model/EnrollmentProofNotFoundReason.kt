package com.gdavidpb.tuindice.enrollmentproof.domain.model

/** Why there is no proof to show, read from what the last sync reported. */
enum class EnrollmentProofNotFoundReason {
	// The university holds an annulled enrollment for this term.
	Annulled,

	// The university reports no enrollment at all.
	NotEnrolled,

	Unknown
}
