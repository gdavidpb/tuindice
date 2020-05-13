package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import java.io.File

open class CreateProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val localStorageRepository: LocalStorageRepository
) : EventUseCase<Unit, Uri>() {
    override suspend fun executeOnBackground(params: Unit): Uri? {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        return localStorageRepository.create(resource).toUri()
    }
}