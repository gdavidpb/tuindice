package com.gdavidpb.tuindice.base.domain.session

/**
 * What a shared object keeps in memory on behalf of the signed-in account: the mirror of a stored
 * preference, a cached snapshot, a credential. Wiping the stored data does not reach it, and a
 * shared object outlives the session, so `ApplicationRepository.clearData()` asks every holder to
 * let go of it in the same step. Bind the holder with `bind<SessionMemory>()` in its Koin module.
 */
interface SessionMemory {
	suspend fun clearSessionMemory()
}
