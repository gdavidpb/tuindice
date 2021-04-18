package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.FlowUseCase
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.math.max

class CountdownUseCase(
        private val settingsRepository: SettingsRepository
) : FlowUseCase<CountdownRequest, Long, Nothing>() {

    object Settings {
        const val DELAY = 1000L
    }

    override suspend fun executeOnBackground(params: CountdownRequest): Flow<Long> {
        val now = Date().time
        val countdownStart = settingsRepository.startCountdown(reset = params.reset)
        val countdownEnd = countdownStart + params.duration
        val leftTime = countdownEnd - now
        val leftRange = max(0, leftTime) downTo 0 step Settings.DELAY

        return leftRange
                .asFlow()
                .onEach { if (it != leftRange.first) delay(Settings.DELAY) }
    }
}