package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class SignOutUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val localStorageRepository: LocalStorageRepository
) : CompletableUseCase<Unit>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.signOut()
        settingsRepository.clear()
        databaseRepository.clearPersistence()
        localStorageRepository.delete("cookies")
        localStorageRepository.delete("enrollment")
    }
}