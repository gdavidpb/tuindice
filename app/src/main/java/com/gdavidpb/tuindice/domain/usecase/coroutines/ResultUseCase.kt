package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*

abstract class ResultUseCase<P, T> : BaseUseCase<P, T, LiveResult<T>>() {
    override suspend fun onStart(liveData: LiveResult<T>) {
        liveData.postLoading()
    }

    override suspend fun onHook(liveData: LiveResult<T>, response: T) {
    }

    override suspend fun onSuccess(liveData: LiveResult<T>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onFailure(liveData: LiveResult<T>, throwable: Throwable) {
        liveData.postThrowable(throwable)
    }

    override suspend fun onCancel(liveData: LiveResult<T>) {
        liveData.postCancel()
    }
}