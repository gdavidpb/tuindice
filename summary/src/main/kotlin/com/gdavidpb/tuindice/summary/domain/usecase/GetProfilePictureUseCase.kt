package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.isObjectNotFound
import com.gdavidpb.tuindice.base.utils.extensions.isTimeout
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.utils.Paths
import java.io.File

@Timeout(key = ConfigKeys.TIME_OUT_PROFILE_PICTURE)
class GetProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val remoteStorageRepository: RemoteStorageRepository,
	private val networkRepository: NetworkRepository
) : ResultUseCase<Unit, String, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit): String {
		val activeUId = authRepository.getActiveAuth().uid
		val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path
		val downloadUrl = runCatching { remoteStorageRepository.resolveResource(resource) }

		return downloadUrl.fold(onSuccess = { url ->
			url.toString()
		}, onFailure = { throwable ->
			if (throwable.isObjectNotFound())
				""
			else
				throw throwable
		})
	}

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}