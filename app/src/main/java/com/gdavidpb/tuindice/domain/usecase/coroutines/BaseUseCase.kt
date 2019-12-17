package com.gdavidpb.tuindice.domain.usecase.coroutines

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<T, Q : MutableLiveData<*>>(
        protected open val backgroundContext: CoroutineContext,
        protected open val foregroundContext: CoroutineContext
) {
    abstract fun execute(params: T, liveData: Q, coroutineScope: CoroutineScope)
}