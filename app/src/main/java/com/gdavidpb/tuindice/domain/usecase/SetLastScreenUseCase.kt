package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

open class SetLastScreenUseCase(
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Int, Any>() {
    override suspend fun executeOnBackground(params: Int) {
        settingsRepository.setLastScreen(screen = params)
    }

    override suspend fun executeOnException(throwable: Throwable): Any? {
        TODO("Not yet implemented")
    }
}