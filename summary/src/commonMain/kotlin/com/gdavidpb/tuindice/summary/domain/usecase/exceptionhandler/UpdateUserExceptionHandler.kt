package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError

class UpdateUserExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<UpdateUserUseCaseError>() {
	override fun parseException(throwable: Throwable): UpdateUserUseCaseError? {
		return when {
			throwable.isNotFound() -> UpdateUserUseCaseError.NotFound
			throwable.isUnavailable() -> UpdateUserUseCaseError.Unavailable
			throwable.isTimeout() -> UpdateUserUseCaseError.Timeout
			throwable.isConnection() -> UpdateUserUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
