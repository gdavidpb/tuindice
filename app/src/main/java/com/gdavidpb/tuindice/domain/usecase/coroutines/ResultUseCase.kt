package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.data.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class ResultUseCase<Q, T>(
        private val backgroundContext: CoroutineContext,
        private val foregroundContext: CoroutineContext
) {
    private val parentJob = Job()

    protected abstract suspend fun executeOnBackground(params: Q): T?

    fun execute(liveData: LiveResult<T>, params: Q) {
        parentJob.cancelChildren()
        parentJob.cancel()

        CoroutineScope(foregroundContext + parentJob).launch {
            liveData.postLoading()

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params)!! }
            }.onSuccess { response ->
                liveData.postSuccess(response)
            }.onFailure { throwable ->
                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    is NullPointerException -> liveData.postEmpty()
                    else -> liveData.postThrowable(throwable)
                }
            }
        }
    }
}