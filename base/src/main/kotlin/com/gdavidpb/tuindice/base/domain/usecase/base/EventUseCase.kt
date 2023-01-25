package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.*

abstract class EventUseCase<P, T, Q> : BaseUseCase<P, T, Q, LiveEvent<T, Q>>() {
    override suspend fun onStart(liveData: LiveEvent<T, Q>) {
        liveData.postLoading()
    }

    override suspend fun onEmpty(liveData: LiveEvent<T, Q>) {
        liveData.postEmpty()
    }

    override suspend fun onSuccess(liveData: LiveEvent<T, Q>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onFailure(liveData: LiveEvent<T, Q>, error: Q?) {
        liveData.postError(error)
    }
}