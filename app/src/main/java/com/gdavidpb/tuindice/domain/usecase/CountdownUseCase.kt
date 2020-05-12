package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.FlowUseCase
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.math.max

@IgnoredExceptions(CancellationException::class)
open class CountdownUseCase(
        private val settingsRepository: SettingsRepository
) : FlowUseCase<CountdownRequest, Long>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: CountdownRequest): Flow<Long> {
        val now = Date().time
        val countdownStart = settingsRepository.startCountdown(reset = params.reset)
        val countdownEnd = countdownStart + params.time
        val leftTime = countdownEnd - now
        val leftSecs = leftTime / 1000
        val leftRange = max(0, leftSecs) downTo 0

        return flow {
            leftRange.forEach { t ->
                emit(t)
                delay(1000)
            }
        }
    }
}