package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

class SetLastScreenUseCase(
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Int, Nothing>() {
    override suspend fun executeOnBackground(params: Int) {
        settingsRepository.setLastScreen(screen = params)
    }
}