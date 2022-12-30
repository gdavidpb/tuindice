package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.isTimeout
import com.gdavidpb.tuindice.summary.utils.ConfigKeys
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.repository.SummaryRepository

@Timeout(key = ConfigKeys.TIME_OUT_PROFILE_PICTURE)
class RemoveProfilePictureUseCase(
	private val networkRepository: NetworkRepository,
	private val summaryRepository: SummaryRepository
) : EventUseCase<Unit, Unit, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit) {
		summaryRepository.removeProfilePicture()
	}

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}