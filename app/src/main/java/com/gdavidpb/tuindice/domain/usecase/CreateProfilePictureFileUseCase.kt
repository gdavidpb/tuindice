package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.StorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import java.io.File
import java.io.IOException

open class CreateProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val storageRepository: StorageRepository<File>
) : EventUseCase<Unit, Uri, ProfilePictureError>() {
    override suspend fun executeOnBackground(params: Unit): Uri? {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        return storageRepository.create(resource).toUri()
    }

    override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
        return when (throwable) {
            is IOException -> ProfilePictureError.IO
            else -> null
        }
    }
}