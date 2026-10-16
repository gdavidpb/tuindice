package com.gdavidpb.tuindice.base.domain.session

/**
 * What a shared object keeps on behalf of the signed-in account outside the stores the wipe
 * empties: in memory (the mirror of a stored preference, a cached snapshot, a credential) or in
 * files of its own (a saved document). Wiping the stored data does not reach it, and a shared
 * object outlives the session, so `ApplicationRepository.clearData()` asks every holder to let go
 * of it in the same step. Bind the holder with `bind<SessionMemory>()` in its Koin module.
 *
 * `clearSessionMemory` runs inside the lock that serializes the end of a session, so it does local work
 * only: it must not make an authenticated request nor ask for another invalidation, because that would
 * wait for that same lock forever.
 */
interface SessionMemory {
	suspend fun clearSessionMemory()
}
