package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class FetchEnrollmentProofError : Error {
	data object Timeout : FetchEnrollmentProofError()
	data object Unavailable : FetchEnrollmentProofError()
	data object OutdatedPassword : FetchEnrollmentProofError()
	data object NotFound : FetchEnrollmentProofError()
	data object UnsupportedFile : FetchEnrollmentProofError()
	class NoConnection(val isNetworkAvailable: Boolean) : FetchEnrollmentProofError()
}