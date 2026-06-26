package com.gdavidpb.tuindice.base.domain.usecase.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun ensureInitialContentLoaded(
	hasLocalContent: suspend () -> Boolean,
	refreshPolicy: InitialContentRefreshPolicy,
	refresh: suspend (InitialContentRefreshRequest) -> Unit
): Flow<InitialContentLoadResult> {
	return flow {
		val hasContent = hasLocalContent()
		if (hasContent) {
			emit(InitialContentLoadResult.Cached)
			if (refreshPolicy == InitialContentRefreshPolicy.MissingOnly) return@flow
		}

		emit(InitialContentLoadResult.RefreshStarted)
		refresh(
			InitialContentRefreshRequest(
				freshness = if (hasContent) {
					InitialContentFreshness.RespectCooldown
				} else {
					InitialContentFreshness.ForceRemote
				}
			)
		)
		emit(InitialContentLoadResult.RefreshSucceeded)
	}
}
