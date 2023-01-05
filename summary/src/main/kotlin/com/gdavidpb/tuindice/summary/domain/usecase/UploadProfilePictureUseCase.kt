package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.isIO
import com.gdavidpb.tuindice.base.utils.extensions.isTimeout
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.utils.ConfigKeys

@Timeout(key = ConfigKeys.TIME_OUT_PROFILE_PICTURE)
class UploadProfilePictureUseCase(
	private val accountRepository: AccountRepository,
	private val encoderRepository: EncoderRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<String, String, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: String): String {
		val encodedPicture = encoderRepository.encodePicture(path = params)

		return accountRepository.uploadProfilePicture(encodedPicture).url
	}

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isIO() -> ProfilePictureError.IO
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}