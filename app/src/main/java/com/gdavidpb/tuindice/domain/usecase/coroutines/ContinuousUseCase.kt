package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class ContinuousUseCase<Q, T>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<Q, LiveContinuous<T>>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: Q, liveData: LiveContinuous<T>)

    override fun execute(liveData: LiveContinuous<T>, params: Q) {
        CoroutineScope(foregroundContext + newJob()).launch {
            liveData.postStart()

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params, liveData) }
            }.onFailure { throwable ->
                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }

            liveData.postComplete()
        }
    }
}