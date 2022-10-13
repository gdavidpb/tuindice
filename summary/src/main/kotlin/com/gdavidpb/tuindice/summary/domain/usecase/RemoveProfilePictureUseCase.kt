package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.isTimeout
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.utils.Paths
import java.io.File

@Timeout(key = ConfigKeys.TIME_OUT_PROFILE_PICTURE)
class RemoveProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val remoteStorageRepository: RemoteStorageRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<Unit, Unit, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit) {
		val activeUId = authRepository.getActiveAuth().uid
		val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path

		remoteStorageRepository.removeResource(resource)
	}

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}