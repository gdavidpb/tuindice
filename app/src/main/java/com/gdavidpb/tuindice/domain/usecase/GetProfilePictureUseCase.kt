package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import com.gdavidpb.tuindice.utils.extensions.isObjectNotFound
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
            throwable.isConnectionIssue() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}