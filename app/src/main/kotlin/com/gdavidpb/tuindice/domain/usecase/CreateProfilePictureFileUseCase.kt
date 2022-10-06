package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.StorageRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.Paths
import java.io.File
import java.io.IOException

class CreateProfilePictureFileUseCase(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : EventUseCase<Unit, Uri, ProfilePictureError>() {
    override suspend fun executeOnBackground(params: Unit): Uri {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path

        return storageRepository.create(resource).toUri()
    }

    override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
        return when {
            throwable is IOException -> ProfilePictureError.IO
            else -> null
        }
    }
}