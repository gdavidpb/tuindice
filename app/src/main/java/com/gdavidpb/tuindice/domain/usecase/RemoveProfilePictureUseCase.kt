package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import java.io.File

open class RemoveProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val remoteStorageRepository: RemoteStorageRepository
) : EventUseCase<Unit, Unit, ProfilePictureError>() {
    override suspend fun executeOnBackground(params: Unit) {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        remoteStorageRepository.removeResource(resource)
    }

    override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
        return when {
            throwable.isConnectionIssue() -> ProfilePictureError.NoConnection
            else -> null
        }
    }
}