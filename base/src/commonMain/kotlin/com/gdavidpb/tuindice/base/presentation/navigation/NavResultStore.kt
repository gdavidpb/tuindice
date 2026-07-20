package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.runtime.mutableStateMapOf

/**
 * Snapshot-backed slot of pending [NavResult]s keyed by the receiver entry's
 * store key; a slot holds at most one pending result.
 */
class NavResultStore {

	private val pendingResults = mutableStateMapOf<String, NavResult>()

	fun publish(receiverStoreKey: String, result: NavResult) {
		pendingResults[receiverStoreKey] = result
	}

	fun peek(storeKey: String): NavResult? = pendingResults[storeKey]

	fun consume(storeKey: String) {
		pendingResults.remove(storeKey)
	}

	fun clear() {
		pendingResults.clear()
	}
}
