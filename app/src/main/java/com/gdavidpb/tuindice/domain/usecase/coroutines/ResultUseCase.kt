package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class ResultUseCase<Q, T>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<Q, LiveResult<T>>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: Q): T?

    override fun execute(params: Q, liveData: LiveResult<T>, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
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