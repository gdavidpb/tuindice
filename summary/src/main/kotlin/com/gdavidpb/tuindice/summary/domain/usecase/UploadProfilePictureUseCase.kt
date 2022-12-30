package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.isIO
import com.gdavidpb.tuindice.base.utils.extensions.isTimeout
import com.gdavidpb.tuindice.summary.utils.ConfigKeys
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.repository.SummaryRepository

@Timeout(key = ConfigKeys.TIME_OUT_PROFILE_PICTURE)
class UploadProfilePictureUseCase(
	private val networkRepository: NetworkRepository,
	private val summaryRepository: SummaryRepository
) : EventUseCase<String, String, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: String): String {
		return summaryRepository.uploadProfilePicture(path = params).url
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