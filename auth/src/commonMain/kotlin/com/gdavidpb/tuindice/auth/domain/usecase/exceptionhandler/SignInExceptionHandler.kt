package com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStage
import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStageException
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isLocked
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isTooManyRequests
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

class SignInExceptionHandler(
	private val networkRepository: NetworkRepository
) : ExceptionHandler<SignInUseCaseError> {
	override fun parseException(throwable: Throwable): SignInUseCaseError? {
		val rootThrowable = (throwable as? AuthenticationStageException)?.cause ?: throwable

		return when {
			throwable is SignInIllegalArgumentException -> throwable.error
			throwable is AuthenticationStageException &&
				throwable.stage == AuthenticationStage.SignInBootstrap &&
				rootThrowable.isUnauthorized() -> SignInUseCaseError.InvalidCredentials

			rootThrowable.isLocked() -> SignInUseCaseError.AccountDisabled
			rootThrowable.isForbidden() -> SignInUseCaseError.Untrusted
			rootThrowable.isUnavailable() || rootThrowable.isTooManyRequests() -> SignInUseCaseError.Unavailable
			rootThrowable.isUnauthorized() -> SignInUseCaseError.AuthenticationFailed
			rootThrowable.isTimeout() -> SignInUseCaseError.Timeout
			rootThrowable.isConnection() -> SignInUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
