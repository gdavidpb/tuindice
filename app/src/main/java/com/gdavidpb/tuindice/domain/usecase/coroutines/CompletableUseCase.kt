package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class CompletableUseCase<Q>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<Q, LiveCompletable>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: Q)

    override fun execute(liveData: LiveCompletable, params: Q) {
        CoroutineScope(foregroundContext + newJob()).launch {
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