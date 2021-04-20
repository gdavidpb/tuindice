package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.StorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.extensions.causes
import com.gdavidpb.tuindice.utils.extensions.isAccountDisabled
import java.io.File
import java.io.IOException

class CreateProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val storageRepository: StorageRepository<File>
) : EventUseCase<Unit, Uri, ProfilePictureError>() {
    override suspend fun executeOnBackground(params: Unit): Uri {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path

        return storageRepository.create(resource).toUri()
    }

    override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
        val causes = throwable.causes()

        return when {
            throwable is IOException -> ProfilePictureError.IO
            else -> null
        }
    }
}