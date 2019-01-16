package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.data.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class CompletableUseCase<Q>(
        private val backgroundContext: CoroutineContext,
        private val foregroundContext: CoroutineContext
) {
    private val parentJob = Job()

    protected abstract suspend fun executeOnBackground(params: Q)

    fun execute(liveData: LiveCompletable, params: Q) {
        parentJob.cancelChildren()
        parentJob.cancel()

        CoroutineScope(foregroundContext + parentJob).launch {
            liveData.postLoading()

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params) }
            }.onSuccess {
                liveData.postComplete()
            }.onFailure { throwable ->
                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }
        }
    }
}