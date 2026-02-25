package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError

class GetUserExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<GetUserUseCaseError>() {
	override fun parseException(throwable: Throwable): GetUserUseCaseError? {
		return when {
			throwable.isUnavailable() -> GetUserUseCaseError.Unavailable
			throwable.isConflict() -> GetUserUseCaseError.OutdatedPassword
			throwable.isTimeout() -> GetUserUseCaseError.Timeout
			throwable.isConnection() -> GetUserUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
