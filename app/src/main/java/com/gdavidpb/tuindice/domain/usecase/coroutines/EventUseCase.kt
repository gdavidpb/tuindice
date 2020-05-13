package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*

abstract class EventUseCase<P, T> : BaseUseCase<P, T, LiveEvent<T>>() {
    override suspend fun onStart(liveData: LiveEvent<T>) {
        liveData.postLoading()
    }

    override suspend fun onHook(liveData: LiveEvent<T>, response: T) {
    }

    override suspend fun onSuccess(liveData: LiveEvent<T>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onFailure(liveData: LiveEvent<T>, throwable: Throwable) {
        liveData.postThrowable(throwable)
    }

    override suspend fun onCancel(liveData: LiveEvent<T>) {
        liveData.postCancel()
    }
}