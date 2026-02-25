package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError

class RemoveProfilePictureExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<ProfilePictureUseCaseError>() {
	override fun parseException(throwable: Throwable): ProfilePictureUseCaseError? {
		return when {
			throwable.isTimeout() -> ProfilePictureUseCaseError.Timeout
			throwable.isConnection() -> ProfilePictureUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
