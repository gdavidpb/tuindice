package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.*

abstract class ResultUseCase<P, T, Q> : BaseUseCase<P, T, Q, LiveResult<T, Q>>() {
	override suspend fun onStart(liveData: LiveResult<T, Q>) {
		liveData.postLoading()
	}

	override suspend fun onEmpty(liveData: LiveResult<T, Q>) {
		liveData.postEmpty()
	}

	override suspend fun onSuccess(liveData: LiveResult<T, Q>, response: T) {
		liveData.postSuccess(response)
	}

	override suspend fun onFailure(liveData: LiveResult<T, Q>, error: Q?) {
		liveData.postError(error)
	}
}