package com.gdavidpb.tuindice.data.source.mutex

import com.gdavidpb.tuindice.base.domain.repository.ConcurrencyRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class MutexDataSource : ConcurrencyRepository {
	private val mutex = ConcurrentHashMap<String, Mutex>()

	override suspend fun <T> withLock(name: String, action: suspend () -> T): T {
		return mutex.getOrPut(name) { Mutex() }.withLock { action() }
	}
}