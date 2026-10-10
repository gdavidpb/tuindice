package com.gdavidpb.tuindice.enrollmentproof.domain.model

data class EnrollmentProof(
	val source: String,
	val content: String,
	// Set by the repository when the saved copy was returned instead of a fresh one.
	val isFromCache: Boolean = false
)