package com.gdavidpb.tuindice.base.presentation.statemachine

class InitialContentRefreshGate<T>(
	private val isCached: (T) -> Boolean,
	private val isRefreshStarted: (T) -> Boolean
) {
	private var hasCachedContent = false

	fun shouldProcess(value: T): Boolean {
		if (isCached(value)) {
			hasCachedContent = true
			return false
		}

		return !hasCachedContent || !isRefreshStarted(value)
	}

	fun shouldProcessError(): Boolean = !hasCachedContent
}
