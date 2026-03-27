package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.record.domain.usecase.error.UpdateQuartersUseCaseError

class UpdateQuartersExceptionHandler(
	private val networkRepository: NetworkRepository
) : ExceptionHandler<UpdateQuartersUseCaseError>() {
	override fun parseException(throwable: Throwable): UpdateQuartersUseCaseError? {
		return when {
			throwable.isUnavailable() -> UpdateQuartersUseCaseError.Unavailable
			throwable.isTimeout() -> UpdateQuartersUseCaseError.Timeout
			throwable.isConnection() -> UpdateQuartersUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
