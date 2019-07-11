package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class SetLastScreenUseCase(
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Int>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Int) {
        settingsRepository.setLastScreen(screen = params)
    }
}