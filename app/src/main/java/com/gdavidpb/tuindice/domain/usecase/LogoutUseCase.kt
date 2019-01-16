package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class LogoutUseCase(
        private val localStorageRepository: LocalStorageRepository,
        private val localDatabaseRepository: LocalDatabaseRepository
) : CompletableUseCase<Unit>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit) {
        localDatabaseRepository.removeActive()
        localStorageRepository.delete("cookies")
    }
}