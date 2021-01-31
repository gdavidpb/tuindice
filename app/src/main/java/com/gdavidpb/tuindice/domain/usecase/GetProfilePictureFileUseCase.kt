package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import java.io.File

open class GetProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val localStorageRepository: LocalStorageRepository
) : EventUseCase<Uri?, Uri, Any>() {
    override suspend fun executeOnBackground(params: Uri?): Uri? {
        return if (params != null) {
            params
        } else {
            val activeUId = authRepository.getActiveAuth().uid
            val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

            return localStorageRepository.get(resource).toUri()
        }
    }

    override suspend fun executeOnException(throwable: Throwable): Any? {
        TODO("Not yet implemented")
    }
}