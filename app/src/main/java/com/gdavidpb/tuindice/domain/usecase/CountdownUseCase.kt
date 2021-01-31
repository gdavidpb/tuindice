package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.FlowUseCase
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.math.max

open class CountdownUseCase(
        private val settingsRepository: SettingsRepository
) : FlowUseCase<CountdownRequest, Long, Any>() {
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

    override suspend fun executeOnException(throwable: Throwable): Any? {
        TODO("Not yet implemented")
    }
}