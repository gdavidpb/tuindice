package com.gdavidpb.tuindice.record.domain.error

sealed class GetEnrollmentError {
	object Timeout : GetEnrollmentError()
	object Unavailable : GetEnrollmentError()
	object AccountDisabled : GetEnrollmentError()
	object OutdatedPassword : GetEnrollmentError()
	object NotFound : GetEnrollmentError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetEnrollmentError()
}