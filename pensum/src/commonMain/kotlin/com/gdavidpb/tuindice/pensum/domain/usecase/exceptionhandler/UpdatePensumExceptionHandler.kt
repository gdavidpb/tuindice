package com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError

class UpdatePensumExceptionHandler(
	private val networkRepository: NetworkRepository
) : ExceptionHandler<UpdatePensumUseCaseError> {
	override fun parseException(throwable: Throwable): UpdatePensumUseCaseError? {
		return when {
			throwable.isNotFound() -> UpdatePensumUseCaseError.NotFound
			throwable.isUnavailable() -> UpdatePensumUseCaseError.Unavailable
			throwable.isTimeout() -> UpdatePensumUseCaseError.Timeout
			throwable.isConnection() ->
				UpdatePensumUseCaseError.NoConnection(networkRepository.isAvailable())

			else -> null
		}
	}
}
