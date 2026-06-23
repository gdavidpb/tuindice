package com.gdavidpb.tuindice.base.domain.usecase.base

sealed interface InitialContentLoadResult {
	data object Cached : InitialContentLoadResult
	data object RefreshStarted : InitialContentLoadResult
	data object RefreshSucceeded : InitialContentLoadResult
}
