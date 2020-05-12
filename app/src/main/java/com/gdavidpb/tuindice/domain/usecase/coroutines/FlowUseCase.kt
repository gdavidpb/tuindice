package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.KEY_USE_CASE
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class FlowUseCase<T, Q>(
        override val backgroundContext: CoroutineContext,
        override val foregroundContext: CoroutineContext
) : BaseUseCase<T, LiveFlow<Q>>(
        backgroundContext, foregroundContext
) {
    protected abstract suspend fun executeOnBackground(params: T): Flow<Q>

    override fun execute(params: T, liveData: LiveFlow<Q>, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            liveData.postStart()

            runCatching {
                withContext(backgroundContext) {
                    executeOnBackground(params).collect { liveData.postNext(it) }
                }
            }.onFailure { throwable ->
                if (!ignoredException(throwable)) {
                    reportingRepository.setString(KEY_USE_CASE, "${this@FlowUseCase::class.simpleName}")
                    reportingRepository.logException(throwable)
                }

                when (throwable) {
                    is CancellationException -> liveData.postCancel()
                    else -> liveData.postThrowable(throwable)
                }
            }

            liveData.postComplete()
        }
    }
}