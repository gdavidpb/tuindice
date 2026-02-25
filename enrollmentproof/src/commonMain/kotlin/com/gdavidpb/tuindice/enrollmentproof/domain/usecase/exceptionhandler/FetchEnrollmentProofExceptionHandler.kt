package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError

class FetchEnrollmentProofExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<FetchEnrollmentProofUseCaseError>() {
	override fun parseException(throwable: Throwable): FetchEnrollmentProofUseCaseError? {
		return when {
			throwable is EnrollmentProofNotFoundException -> FetchEnrollmentProofUseCaseError.NotFound
			throwable is UnsupportedOperationException -> FetchEnrollmentProofUseCaseError.UnsupportedFile
			throwable.isNotFound() -> FetchEnrollmentProofUseCaseError.NotFound
			throwable.isUnavailable() -> FetchEnrollmentProofUseCaseError.Unavailable
			throwable.isConflict() -> FetchEnrollmentProofUseCaseError.OutdatedPassword
			throwable.isTimeout() -> FetchEnrollmentProofUseCaseError.Timeout
			throwable.isConnection() -> FetchEnrollmentProofUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
