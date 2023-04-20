package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error

sealed class GetEnrollmentError {
	object Timeout : GetEnrollmentError()
	object Unavailable : GetEnrollmentError()
	object AccountDisabled : GetEnrollmentError()
	object OutdatedPassword : GetEnrollmentError()
	object NotFound : GetEnrollmentError()
	object UnsupportedFile : GetEnrollmentError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetEnrollmentError()
}