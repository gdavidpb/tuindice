package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.*
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
                toggle(
                        release = { Crashlytics.logException(throwable) },
                        debug = { throwable.printStackTrace() }
                )

                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }
        }
    }
}