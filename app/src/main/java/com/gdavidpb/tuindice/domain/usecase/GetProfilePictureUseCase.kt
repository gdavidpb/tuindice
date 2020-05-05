package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import com.gdavidpb.tuindice.utils.extensions.isObjectNotFound
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import java.io.File

@IgnoredExceptions(CancellationException::class)
open class GetProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val remoteStorageRepository: RemoteStorageRepository
) : ResultUseCase<Unit, String>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): String? {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path
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
}