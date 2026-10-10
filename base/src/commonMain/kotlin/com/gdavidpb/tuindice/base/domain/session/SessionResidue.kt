package com.gdavidpb.tuindice.base.domain.session

/**
 * Files of its own that a shared object keeps for the signed-in account and that may be left on the
 * device after that account is gone: a saved document (the enrollment proofs), or a photo written
 * for an upload the process never finished (the iOS normalized profile picture). A person who
 * signed out before the sign-out wipe covered them, or whose app died mid-upload, still has them,
 * and the next one to sign in on the phone would find them. Each holder lists its own files here;
 * there is no single owner.
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
