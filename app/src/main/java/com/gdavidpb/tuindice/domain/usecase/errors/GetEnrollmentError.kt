package com.gdavidpb.tuindice.domain.usecase.errors

sealed class GetEnrollmentError {
    object NotFound : GetEnrollmentError()
    object NotEnrolled : GetEnrollmentError()
    object InvalidCredentials : GetEnrollmentError()
    object NoConnection : GetEnrollmentError()
}
