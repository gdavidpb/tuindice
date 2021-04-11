package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
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
            throwable.isConnectionIssue() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}