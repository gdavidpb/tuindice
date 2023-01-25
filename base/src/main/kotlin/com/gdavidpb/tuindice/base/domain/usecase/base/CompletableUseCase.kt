package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.postComplete
import com.gdavidpb.tuindice.base.utils.extension.postError
import com.gdavidpb.tuindice.base.utils.extension.postLoading

abstract class CompletableUseCase<P, Q> : BaseUseCase<P, Unit, Q, LiveCompletable<Q>>() {
    override suspend fun onStart(liveData: LiveCompletable<Q>) {
        liveData.postLoading()
    }

    override suspend fun onEmpty(liveData: LiveCompletable<Q>) {}

    override suspend fun onSuccess(liveData: LiveCompletable<Q>, response: Unit) {
        liveData.postComplete()
    }

    override suspend fun onFailure(liveData: LiveCompletable<Q>, error: Q?) {
        liveData.postError(error)
    }
}