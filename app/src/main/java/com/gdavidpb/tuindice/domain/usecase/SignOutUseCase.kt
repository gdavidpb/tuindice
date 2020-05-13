package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.utils.extensions.restartKoinModules

open class SignOutUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val localStorageRepository: LocalStorageRepository
) : CompletableUseCase<Unit>() {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.signOut()
        settingsRepository.clear()
        databaseRepository.close()
        databaseRepository.clearPersistence()
        localStorageRepository.clear()

        restartKoinModules()
    }
}