package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.postComplete
import com.gdavidpb.tuindice.base.utils.extension.postError
import com.gdavidpb.tuindice.base.utils.extension.postLoading

abstract class CompletableUseCase<P, E> : BaseUseCase<P, Unit, E, LiveCompletable<E>>() {
    override suspend fun onStart(liveData: LiveCompletable<E>) {
        liveData.postLoading()
    }

    override suspend fun onEmpty(liveData: LiveCompletable<E>) {}

    override suspend fun onSuccess(liveData: LiveCompletable<E>, response: Unit) {
        liveData.postComplete()
    }

    override suspend fun onError(liveData: LiveCompletable<E>, error: E?) {
        liveData.postError(error)
    }
}