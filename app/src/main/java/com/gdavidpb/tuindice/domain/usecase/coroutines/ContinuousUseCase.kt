package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class ContinuousUseCase<T, Q>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<T, LiveContinuous<Q>>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: T, liveData: LiveContinuous<Q>)

    override fun execute(params: T, liveData: LiveContinuous<Q>, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            liveData.postStart()

            runCatching {
                withContext(backgroundContext) { executeOnBackground(params, liveData) }
            }.onFailure { throwable ->
                Crashlytics.logException(throwable)

                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }

            liveData.postComplete()
        }
    }
}