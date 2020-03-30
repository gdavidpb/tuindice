package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import kotlinx.coroutines.Dispatchers
import java.io.File

open class CreateProfilePictureFileUseCase(
        private val authRepository: AuthRepository,
        private val localStorageRepository: LocalStorageRepository
) : ResultUseCase<Unit, Uri>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): Uri? {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        return localStorageRepository.create(resource).toUri()
    }
}