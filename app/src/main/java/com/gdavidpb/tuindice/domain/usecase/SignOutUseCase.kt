package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import java.io.File

class SignOutUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val storageRepository: StorageRepository<File>,
        private val dependenciesRepository: DependenciesRepository
) : CompletableUseCase<Unit, Nothing>() {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.signOut()
        settingsRepository.clear()
        databaseRepository.close()
        databaseRepository.clearCache()
        storageRepository.clear()
        dependenciesRepository.restart()
    }
}