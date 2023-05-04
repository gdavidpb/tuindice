package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError

class GetAccountExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<GetAccountError>() {
	override fun parseException(throwable: Throwable): GetAccountError? {
		return when {
			throwable.isUnavailable() -> GetAccountError.Unavailable
			throwable.isConflict() -> GetAccountError.OutdatedPassword
			throwable.isTimeout() -> GetAccountError.Timeout
			throwable.isConnection() -> GetAccountError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}