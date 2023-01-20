package com.gdavidpb.tuindice.base.domain.repository

interface ConcurrencyRepository {
	suspend fun <T> withLock(name: String, action: suspend () -> T): T
}