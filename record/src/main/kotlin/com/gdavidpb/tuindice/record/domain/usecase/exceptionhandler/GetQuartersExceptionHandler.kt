package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError

class GetQuartersExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<GetQuartersError>() {
	override fun parseException(throwable: Throwable): GetQuartersError? {
		return when {
			throwable.isUnavailable() -> GetQuartersError.Unavailable
			throwable.isConflict() -> GetQuartersError.OutdatedPassword
			throwable.isTimeout() -> GetQuartersError.Timeout
			throwable.isConnection() -> GetQuartersError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}