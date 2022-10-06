package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase

class SetLastScreenUseCase(
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Int, Nothing>() {
    override suspend fun executeOnBackground(params: Int) {
        settingsRepository.setLastScreen(screen = params)
    }
}