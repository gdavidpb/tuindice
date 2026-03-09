package com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

class SignInExceptionHandler(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<SignInUseCaseError>() {
	override fun parseException(throwable: Throwable): SignInUseCaseError? {
		return when {
			throwable is SignInIllegalArgumentException -> throwable.error
			throwable.isForbidden() -> SignInUseCaseError.UserDisabled
			throwable.isUnavailable() -> SignInUseCaseError.Unavailable
			throwable.isUnauthorized() -> SignInUseCaseError.InvalidCredentials
			throwable.isTimeout() -> SignInUseCaseError.Timeout
			throwable.isConnection() -> SignInUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
