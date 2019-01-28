package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ContinuousUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*

open class CountdownUseCase(
        private val settingsRepository: SettingsRepository
) : ContinuousUseCase<Unit, Long>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit, onNext: (Long) -> Unit) {
        var now: Date
        val countdown = settingsRepository.getCountdown()

        do {
            now = Date()

            val left = Math.max(0, countdown - now.time)

            withContext(foregroundContext) {
                onNext(left)
            }

            delay(1000)
        } while (left > 0)
    }
}