package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ContinuousUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*

open class CountdownUseCase(
        private val settingsRepository: SettingsRepository
) : ContinuousUseCase<Boolean, Long>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean, onNext: (Long) -> Unit) {
        val savedCountdown = settingsRepository.getCountdown()

        val (countdownExists, forceRestart) = listOf(savedCountdown != 0L, params)

        val currentCountdown = if (!countdownExists || forceRestart) settingsRepository.startCountdown() else savedCountdown

        var left: Long

        do {
            left = (currentCountdown - Date().time)

            withContext(foregroundContext) {
                onNext(Math.max(0, left))
            }

            delay(1000)
        } while (left > 0)
    }
}