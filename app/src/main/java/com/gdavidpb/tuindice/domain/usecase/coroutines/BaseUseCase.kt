package com.gdavidpb.tuindice.domain.usecase.coroutines

import androidx.lifecycle.LiveData
import com.gdavidpb.tuindice.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.utils.ReportKeys
import com.gdavidpb.tuindice.utils.extensions.getTimeoutKey
import com.gdavidpb.tuindice.utils.extensions.hasTimeoutKey
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseUseCase<P, T, Q, L : LiveData<*>>(
    protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
    protected open val foregroundDispatcher: CoroutineDispatcher = Dispatchers.Main
) : KoinComponent {
    private val reportingRepository by inject<ReportingRepository>()
    private val configRepository by inject<ConfigRepository>()

    abstract suspend fun executeOnBackground(params: P): T?

    open suspend fun executeOnResponse(liveData: L, response: T) {}
    open suspend fun executeOnException(throwable: Throwable): Q? = null

    protected abstract suspend fun onStart(liveData: L)
    protected abstract suspend fun onEmpty(liveData: L)
    protected abstract suspend fun onSuccess(liveData: L, response: T)
    protected abstract suspend fun onFailure(liveData: L, error: Q?)

    fun execute(params: P, liveData: L, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundDispatcher) {
            onStart(liveData)

            runCatching {
                withContext(backgroundDispatcher) {
                    val response = if (hasTimeoutKey()) {
                        val key = getTimeoutKey()
                        val timeMillis = configRepository.getLong(key)

                        withTimeout(timeMillis) { executeOnBackground(params) }
                    } else {
                        executeOnBackground(params)
                    }

                    response?.also { executeOnResponse(liveData, it) }
                }
            }.onSuccess { response ->
                if (response != null)
                    onSuccess(liveData, response)
                else
                    onEmpty(liveData)
            }.onFailure { throwable ->
                val error = runCatching { executeOnException(throwable) }.getOrNull()

                reportFailure(throwable = throwable, isHandled = (error != null))

                onFailure(liveData, error)
            }
        }
    }

    private fun reportFailure(throwable: Throwable, isHandled: Boolean) {
        reportingRepository.setCustomKey(ReportKeys.USE_CASE, "${this@BaseUseCase::class.simpleName}")
        reportingRepository.setCustomKey(ReportKeys.HANDLED, isHandled)
        reportingRepository.logException(throwable)
    }
}