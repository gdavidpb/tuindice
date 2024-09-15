package com.gdavidpb.tuindice.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
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
		return when (throwable) {
			is ServicesUnavailableException ->
				StartUpUseCaseError.NoServices(throwable.servicesStatus)

			else -> {
				noAwait {
					applicationRepository.clearData()
				}

				null
			}
		}
	}
}