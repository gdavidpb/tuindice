package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*

abstract class CompletableUseCase<P, Q> : BaseUseCase<P, Unit, Q, LiveCompletable<Q>>() {
    override suspend fun onStart(liveData: LiveCompletable<Q>) {
        liveData.postLoading()
    }

    override suspend fun onHook(liveData: LiveCompletable<Q>, response: Unit) {
    }

    override suspend fun onSuccess(liveData: LiveCompletable<Q>, response: Unit) {
        liveData.postComplete()
    }

    override suspend fun onFailure(liveData: LiveCompletable<Q>, error: Q?) {
        liveData.postError(error)
    }

    override suspend fun onCancel(liveData: LiveCompletable<Q>) {
        liveData.postCancel()
    }
}