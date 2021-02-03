package com.gdavidpb.tuindice.domain.usecase.coroutines

import androidx.lifecycle.LiveData
import com.gdavidpb.tuindice.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.utils.KEY_HANDLED
import com.gdavidpb.tuindice.utils.KEY_USE_CASE
import com.gdavidpb.tuindice.utils.extensions.isIgnoredFromExceptionReporting
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<P, T, Q, L : LiveData<*>>(
        protected open val backgroundContext: CoroutineContext = Dispatchers.IO,
        protected open val foregroundContext: CoroutineContext = Dispatchers.Main
) : KoinComponent {
    private val reportingRepository by inject<ReportingRepository>()

    abstract suspend fun executeOnBackground(params: P): T?

    open suspend fun executeOnException(throwable: Throwable): Q? = null
    open suspend fun executeOnHook(liveData: L, response: T) {}

    protected abstract suspend fun onStart(liveData: L)
    protected abstract suspend fun onSuccess(liveData: L, response: T)
    protected abstract suspend fun onFailure(liveData: L, error: Q?)
    protected abstract suspend fun onCancel(liveData: L)

    fun execute(params: P, liveData: L, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            onStart(liveData)

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params)!!.also { executeOnHook(liveData, it) } }
            }.onSuccess { response ->
                onSuccess(liveData, response)
            }.onFailure { throwable ->
                when (throwable) {
                    is CancellationException -> onCancel(liveData)
                    else -> {
                        val error = runCatching { executeOnException(throwable) }.getOrNull()

                        reportFailure(throwable = throwable, isHandled = error != null)

                        onFailure(liveData, error)
                    }
                }
            }
        }
    }

    private fun reportFailure(throwable: Throwable, isHandled: Boolean) {
        if (!isIgnoredFromExceptionReporting(throwable)) {
            reportingRepository.setCustomKey(KEY_USE_CASE, "${this@BaseUseCase::class.simpleName}")
            reportingRepository.setCustomKey(KEY_HANDLED, isHandled)
            reportingRepository.logException(throwable)
        }
    }
}