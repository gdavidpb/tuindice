package com.gdavidpb.tuindice.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError

class StartUpExceptionHandler : ExceptionHandler<StartUpUseCaseError> {
	override fun parseException(throwable: Throwable): StartUpUseCaseError? {
		val isNotServicesException = throwable::class.simpleName == "GooglePlayServicesNotAvailableException"

		return if (isNotServicesException)
			StartUpUseCaseError.NoServices
		else
			null
	}
}
