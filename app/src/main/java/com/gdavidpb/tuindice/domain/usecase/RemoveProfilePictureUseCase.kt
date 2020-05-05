package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import java.io.File

@IgnoredExceptions(CancellationException::class)
open class RemoveProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val remoteStorageRepository: RemoteStorageRepository
) : EventUseCase<Unit, Unit>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit) {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        remoteStorageRepository.removeResource(resource)
    }
}