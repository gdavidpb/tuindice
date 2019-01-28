package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class ResultUseCase<Q, T>(
        protected val backgroundContext: CoroutineContext,
        protected val foregroundContext: CoroutineContext
) {
    private var parentJob = Job()

    protected abstract suspend fun executeOnBackground(params: Q): T?

    fun execute(liveData: LiveResult<T>, params: Q) {
        resetJob()

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