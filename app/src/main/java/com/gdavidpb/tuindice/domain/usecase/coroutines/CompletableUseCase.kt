package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.data.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class CompletableUseCase<Q>(
        private val backgroundContext: CoroutineContext,
        private val foregroundContext: CoroutineContext
) {
    private var parentJob = Job()

    protected abstract suspend fun executeOnBackground(params: Q)

    fun execute(liveData: LiveCompletable, params: Q) {
        resetJob()

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

    private fun resetJob() {
        parentJob = parentJob.run {
            when {
                isActive -> {
                    cancelChildren()
                    cancel()

                    Job()
                }
                isCancelled || isCompleted -> Job()
                else -> parentJob
            }
        }
    }
}