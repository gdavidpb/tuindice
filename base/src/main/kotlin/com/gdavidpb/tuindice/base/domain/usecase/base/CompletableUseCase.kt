package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.postComplete
import com.gdavidpb.tuindice.base.utils.extensions.postError
import com.gdavidpb.tuindice.base.utils.extensions.postLoading

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