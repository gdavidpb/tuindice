package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isPayloadTooLarge
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnsupportedMediaType
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError

class UploadProfilePictureExceptionHandler(
	private val networkRepository: NetworkRepository
) : ExceptionHandler<ProfilePictureUseCaseError>() {
	override fun parseException(throwable: Throwable): ProfilePictureUseCaseError? {
		return when {
			throwable is IllegalArgumentException -> ProfilePictureUseCaseError.InvalidImage
			throwable.isUnsupportedMediaType() -> ProfilePictureUseCaseError.InvalidImage
			throwable.isPayloadTooLarge() -> ProfilePictureUseCaseError.SizeExceeded
			throwable.isTimeout() -> ProfilePictureUseCaseError.Timeout
			throwable.isConnection() -> ProfilePictureUseCaseError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
