package com.gdavidpb.tuindice.domain.usecase.coroutines

import androidx.lifecycle.LiveData
import com.gdavidpb.tuindice.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.utils.KEY_USE_CASE
import com.gdavidpb.tuindice.utils.extensions.ignoredException
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<P, T, L : LiveData<*>>(
        protected open val backgroundContext: CoroutineContext = Dispatchers.IO,
        protected open val foregroundContext: CoroutineContext = Dispatchers.Main
) : KoinComponent {
    private val reportingRepository by inject<ReportingRepository>()

    abstract suspend fun executeOnBackground(params: P): T?

    protected abstract suspend fun onStart(liveData: L)
    protected abstract suspend fun onHook(liveData: L, response: T)
    protected abstract suspend fun onSuccess(liveData: L, response: T)
    protected abstract suspend fun onFailure(liveData: L, throwable: Throwable)
    protected abstract suspend fun onCancel(liveData: L)

    fun execute(params: P, liveData: L, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            onStart(liveData)

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params)!!.also { onHook(liveData, it) } }
            }.onSuccess { response ->
                onSuccess(liveData, response)
            }.onFailure { throwable ->
                if (throwable !is CancellationException) {
                    if (!ignoredException(throwable)) {
                        reportingRepository.setString(KEY_USE_CASE, "${this@BaseUseCase::class.simpleName}")
                        reportingRepository.logException(throwable)
                    }

                    onFailure(liveData, throwable)
                } else {
                    onCancel(liveData)
                }
            }
        }
    }
}