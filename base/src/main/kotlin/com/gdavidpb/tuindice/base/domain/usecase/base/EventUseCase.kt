package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.*

abstract class EventUseCase<P, T, E> : BaseUseCase<P, T, E, LiveEvent<T, E>>() {
    override suspend fun onStart(liveData: LiveEvent<T, E>) {
        liveData.postLoading()
    }

    override suspend fun onEmpty(liveData: LiveEvent<T, E>) {
        liveData.postEmpty()
    }

    override suspend fun onSuccess(liveData: LiveEvent<T, E>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onError(liveData: LiveEvent<T, E>, error: E?) {
        liveData.postError(error)
    }
}