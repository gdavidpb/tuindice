package com.gdavidpb.tuindice.domain.usecase.coroutines

import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

abstract class FlowUseCase<P, T, Q> : BaseUseCase<P, Flow<T>, Q, LiveFlow<T, Q>>() {
    override suspend fun onStart(liveData: LiveFlow<T, Q>) {
        liveData.postStart()
    }

    override suspend fun onEmpty(liveData: LiveFlow<T, Q>) {}

    override suspend fun executeOnHook(liveData: LiveFlow<T, Q>, response: Flow<T>) {
        response.collect { liveData.postNext(it) }
    }

    override suspend fun onSuccess(liveData: LiveFlow<T, Q>, response: Flow<T>) {
        liveData.postComplete()
    }

    override suspend fun onFailure(liveData: LiveFlow<T, Q>, error: Q?) {
        liveData.postError(error)
    }

    override suspend fun onCancel(liveData: LiveFlow<T, Q>) {
        liveData.postCancel()
    }
}