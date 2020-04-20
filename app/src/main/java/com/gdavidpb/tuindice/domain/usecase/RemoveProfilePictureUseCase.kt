package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import kotlinx.coroutines.Dispatchers

open class RemoveProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : EventUseCase<Unit, Unit>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.setHasProfilePicture(uid = activeUId, hasProfilePicture = false)
    }
}