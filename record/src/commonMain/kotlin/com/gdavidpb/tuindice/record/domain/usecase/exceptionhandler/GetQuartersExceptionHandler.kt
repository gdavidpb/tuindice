package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError

class GetQuartersExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<GetQuartersUseCaseError>() {
	override fun parseException(throwable: Throwable): GetQuartersUseCaseError? {
		return when {
			throwable.isUnavailable() -> GetQuartersUseCaseError.Unavailable
			throwable.isTimeout() -> GetQuartersUseCaseError.Timeout
			throwable.isConnection() -> GetQuartersUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
