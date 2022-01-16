package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.StorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.Paths
import java.io.File

class GetProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val storageRepository: StorageRepository
) : EventUseCase<Uri?, Uri, Nothing>() {
    override suspend fun executeOnBackground(params: Uri?): Uri {
        return if (params != null) {
            params
        } else {
            val activeUId = authRepository.getActiveAuth().uid
            val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path

            return storageRepository.get(resource).toUri()
        }
    }
}