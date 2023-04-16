package com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError

class StartUpExceptionHandler(
	private val networkRepository: NetworkRepository,
	private val applicationRepository: ApplicationRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<StartUpError>() {
	override fun parseException(throwable: Throwable): StartUpError? {
		return when {
			throwable is ServicesUnavailableException -> StartUpError.NoServices(throwable.servicesStatus)
			throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
			else -> {
				noAwait {
					applicationRepository.clearData()
				}

				null
			}
		}
	}
}