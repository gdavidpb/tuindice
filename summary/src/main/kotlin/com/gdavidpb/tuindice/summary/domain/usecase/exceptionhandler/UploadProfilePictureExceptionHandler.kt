package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isIO
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError

class UploadProfilePictureExceptionHandler(
	private val networkRepository: NetworkRepository
) : ExceptionHandler<ProfilePictureError> {
	override fun parseException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isIO() -> ProfilePictureError.IO
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}