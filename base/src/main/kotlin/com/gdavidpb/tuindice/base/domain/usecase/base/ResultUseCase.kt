package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.utils.extension.*

abstract class ResultUseCase<P, T, E> : BaseUseCase<P, T, E, LiveResult<T, E>>() {
	override suspend fun onStart(liveData: LiveResult<T, E>) {
		liveData.postLoading()
	}

	override suspend fun onEmpty(liveData: LiveResult<T, E>) {
		liveData.postEmpty()
	}

	override suspend fun onSuccess(liveData: LiveResult<T, E>, response: T) {
		liveData.postSuccess(response)
	}

	override suspend fun onError(liveData: LiveResult<T, E>, error: E?) {
		liveData.postError(error)
	}
}