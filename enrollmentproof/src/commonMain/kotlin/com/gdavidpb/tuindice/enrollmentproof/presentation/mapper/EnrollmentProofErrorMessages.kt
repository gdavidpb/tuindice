package com.gdavidpb.tuindice.enrollmentproof.presentation.mapper

import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider

internal suspend fun FetchEnrollmentProofUseCaseError?.toErrorMessage(
	textProvider: EnrollmentProofTextProvider
): String {
	return when (this) {
		is FetchEnrollmentProofUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				textProvider.serviceUnavailable()
			else
				textProvider.networkUnavailable()

		is FetchEnrollmentProofUseCaseError.NotFound ->
			textProvider.enrollmentNotFound()

		is FetchEnrollmentProofUseCaseError.EnrollmentAnnulled ->
			textProvider.enrollmentAnnulled()

		is FetchEnrollmentProofUseCaseError.NotEnrolled ->
			textProvider.notEnrolled()

		is FetchEnrollmentProofUseCaseError.UnsupportedFile ->
			textProvider.enrollmentUnsupported()

		is FetchEnrollmentProofUseCaseError.Timeout ->
			textProvider.timeout()

		is FetchEnrollmentProofUseCaseError.Unavailable ->
			textProvider.serviceUnavailable()

		else ->
			textProvider.defaultError()
	}
}

// There is nothing to try again when the university says there is no proof to give.
internal fun FetchEnrollmentProofUseCaseError?.canRetry(): Boolean {
	return when (this) {
		is FetchEnrollmentProofUseCaseError.NotFound,
		is FetchEnrollmentProofUseCaseError.EnrollmentAnnulled,
		is FetchEnrollmentProofUseCaseError.NotEnrolled -> false

		else -> true
	}
}
