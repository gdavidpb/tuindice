package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

abstract class FlowUseCase<P, T> : BaseUseCase<P, Flow<T>, LiveFlow<T>>() {
    override suspend fun onStart(liveData: LiveFlow<T>) {
        liveData.postStart()
    }

    override suspend fun onHook(liveData: LiveFlow<T>, response: Flow<T>) {
        response.collect { liveData.postNext(it) }
    }

    override suspend fun onSuccess(liveData: LiveFlow<T>, response: Flow<T>) {
        liveData.postComplete()
    }

    override suspend fun onFailure(liveData: LiveFlow<T>, throwable: Throwable) {
        liveData.postThrowable(throwable)
    }

    override suspend fun onCancel(liveData: LiveFlow<T>) {
        liveData.postCancel()
    }
}