package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*

abstract class ResultUseCase<P, T, Q> : BaseUseCase<P, T, Q, LiveResult<T, Q>>() {
    override suspend fun onStart(liveData: LiveResult<T, Q>) {
        liveData.postLoading()
    }

    override suspend fun onSuccess(liveData: LiveResult<T, Q>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onFailure(liveData: LiveResult<T, Q>, error: Q?) {
        liveData.postError(error)
    }

    override suspend fun onCancel(liveData: LiveResult<T, Q>) {
        liveData.postCancel()
    }
}