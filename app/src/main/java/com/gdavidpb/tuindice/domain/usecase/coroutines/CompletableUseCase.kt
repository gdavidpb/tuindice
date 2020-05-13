package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*

abstract class CompletableUseCase<P> : BaseUseCase<P, Unit, LiveCompletable>() {
    override suspend fun onStart(liveData: LiveCompletable) {
        liveData.postLoading()
    }

    override suspend fun onHook(liveData: LiveCompletable, response: Unit) {
    }

    override suspend fun onSuccess(liveData: LiveCompletable, response: Unit) {
        liveData.postComplete()
    }

    override suspend fun onFailure(liveData: LiveCompletable, throwable: Throwable) {
        liveData.postThrowable(throwable)
    }

    override suspend fun onCancel(liveData: LiveCompletable) {
        liveData.postCancel()
    }
}