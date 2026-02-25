package com.gdavidpb.tuindice.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError

class StartUpExceptionHandler(
	private val applicationRepository: ApplicationRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<StartUpUseCaseError>() {
	override fun parseException(throwable: Throwable): StartUpUseCaseError? {
		if (isGooglePlayServicesNotAvailableException(throwable)) {
			return StartUpUseCaseError.NoServices
		}

		noAwait {
			applicationRepository.clearData()
		}

		return null
	}
}

private fun isGooglePlayServicesNotAvailableException(throwable: Throwable): Boolean {
	return throwable::class.simpleName == "GooglePlayServicesNotAvailableException"
}
