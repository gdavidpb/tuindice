package com.gdavidpb.tuindice.base.domain.session

/**
 * Files of its own that a shared object keeps for the signed-in account and that a version before
 * the sign-out wipe covered it left on the device (a saved document). A person who signed out with
 * that version still has them, and the next one to sign in on the phone would find them.
 *
 * Narrower than [SessionMemory] on purpose: [SessionMemory] is what a sign-out lets go of, all of
 * it, and includes state that is valid without a session (the usage-data consent can be given on
 * the sign-in screen). The start-up only asks for this one, through
 * `ApplicationRepository.clearSessionResidue()`, and only when there is no active session; with a
 * session these files are the user's own. It runs on every such start, so an implementation must be
 * idempotent and cheap when there is nothing to remove. Bind the holder with
 * `bind<SessionResidue>()` in its Koin module, next to its `SessionMemory` binding if it has one.
 */
interface SessionResidue {
	suspend fun clearSessionResidue()
}
