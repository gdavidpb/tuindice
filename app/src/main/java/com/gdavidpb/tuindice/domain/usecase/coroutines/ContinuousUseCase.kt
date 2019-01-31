package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.LiveContinuous
import com.gdavidpb.tuindice.utils.postCancel
import com.gdavidpb.tuindice.utils.postComplete
import com.gdavidpb.tuindice.utils.postThrowable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class ContinuousUseCase<Q, T>(
        protected val backgroundContext: CoroutineContext,
        protected val foregroundContext: CoroutineContext
) {
    protected abstract suspend fun executeOnBackground(params: Q, onNext: (T) -> Unit)

    private var parentJob = Job()

    fun execute(liveData: LiveContinuous<T>, params: Q, onNext: (T) -> Unit) {
        CoroutineScope(foregroundContext + newJob()).launch {
            runCatching {
                withContext(backgroundContext) { executeOnBackground(params, onNext) }
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

    private fun newJob(): Job {
        parentJob = parentJob.run {
            cancelChildren()
            cancel()

            Job()
        }

        return parentJob
    }
}