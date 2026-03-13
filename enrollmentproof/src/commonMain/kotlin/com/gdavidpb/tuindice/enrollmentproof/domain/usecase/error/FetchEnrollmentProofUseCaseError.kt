package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface FetchEnrollmentProofUseCaseError : UseCaseError {
	data object Timeout : FetchEnrollmentProofUseCaseError
	data object Unavailable : FetchEnrollmentProofUseCaseError
	data object OutdatedCredentials : FetchEnrollmentProofUseCaseError
	data object NotFound : FetchEnrollmentProofUseCaseError
	data object UnsupportedFile : FetchEnrollmentProofUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : FetchEnrollmentProofUseCaseError
}
