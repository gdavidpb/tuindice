package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError

class FetchEnrollmentProofExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<FetchEnrollmentProofError>() {
	override fun parseException(throwable: Throwable): FetchEnrollmentProofError? {
		return when {
			throwable is EnrollmentProofNotFoundException -> FetchEnrollmentProofError.NotFound
			throwable is UnsupportedOperationException -> FetchEnrollmentProofError.UnsupportedFile
			throwable.isForbidden() -> FetchEnrollmentProofError.AccountDisabled
			throwable.isNotFound() -> FetchEnrollmentProofError.NotFound
			throwable.isUnavailable() -> FetchEnrollmentProofError.Unavailable
			throwable.isConflict() -> FetchEnrollmentProofError.OutdatedPassword
			throwable.isTimeout() -> FetchEnrollmentProofError.Timeout
			throwable.isConnection() -> FetchEnrollmentProofError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}