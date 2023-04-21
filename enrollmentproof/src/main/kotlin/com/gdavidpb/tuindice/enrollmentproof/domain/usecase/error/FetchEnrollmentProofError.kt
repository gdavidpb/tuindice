package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error

sealed class FetchEnrollmentProofError {
	object Timeout : FetchEnrollmentProofError()
	object Unavailable : FetchEnrollmentProofError()
	object AccountDisabled : FetchEnrollmentProofError()
	object OutdatedPassword : FetchEnrollmentProofError()
	object NotFound : FetchEnrollmentProofError()
	object UnsupportedFile : FetchEnrollmentProofError()
	class NoConnection(val isNetworkAvailable: Boolean) : FetchEnrollmentProofError()
}