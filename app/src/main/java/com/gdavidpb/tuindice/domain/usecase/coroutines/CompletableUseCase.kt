package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class CompletableUseCase<Q>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<Q, LiveCompletable>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: Q)

    override fun execute(params: Q, liveData: LiveCompletable, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            liveData.postLoading()

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params) }
            }.onSuccess {
                liveData.postComplete()
            }.onFailure { throwable ->
                if (!ignoredException(throwable)) reportingRepository.logException(throwable)

                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }
        }
    }
}