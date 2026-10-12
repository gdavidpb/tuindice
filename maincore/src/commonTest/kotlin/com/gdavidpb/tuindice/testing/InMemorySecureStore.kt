package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import kotlinx.coroutines.CompletableDeferred

// A secure store that lives in memory. A read can be made to wait ([gateReads]) to hold whoever is
// reading it, the way a slow keystore does.
internal class InMemorySecureStore : SecureKeyValueDataRepository {
	val values = mutableMapOf<String, String>()
	var clearCalls = 0
		private set

	private var readGate: CompletableDeferred<Unit>? = null

	fun gateReads(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate -> readGate = gate }

	override suspend fun getString(key: String): String? {
		readGate?.let { gate ->
			readGate = null
			gate.await()
		}

		return values[key]
	}

	override suspend fun putString(key: String, value: String) {
		values[key] = value
	}

	override suspend fun remove(key: String) {
		values.remove(key)
	}

	override suspend fun clear() {
		clearCalls++
		values.clear()
	}
}
