package com.gdavidpb.tuindice.domain.usecase.errors

sealed class GetEnrollmentError {
    object Timeout : GetEnrollmentError()
    object NotFound : GetEnrollmentError()
    object NotEnrolled : GetEnrollmentError()
    class NoConnection(val isNetworkAvailable: Boolean) : GetEnrollmentError()
}
