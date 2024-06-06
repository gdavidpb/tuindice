package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountUseCaseError

class GetAccountExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<GetAccountUseCaseError>() {
	override fun parseException(throwable: Throwable): GetAccountUseCaseError? {
		return when {
			throwable.isUnavailable() -> GetAccountUseCaseError.Unavailable
			throwable.isConflict() -> GetAccountUseCaseError.OutdatedPassword
			throwable.isTimeout() -> GetAccountUseCaseError.Timeout
			throwable.isConnection() -> GetAccountUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}