package com.gdavidpb.tuindice.domain.usecase.errors

sealed class GetEnrollmentError {
    object NotFound : GetEnrollmentError()
    object NotEnrolled : GetEnrollmentError()
    @Deprecated("Rename to CredentialsChanged")
    object InvalidCredentials : GetEnrollmentError()
    class NoConnection(val isNetworkAvailable: Boolean) : GetEnrollmentError()
}
