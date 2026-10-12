package com.gdavidpb.tuindice.base.domain.repository

interface SessionEndRepository {
	// The local end of a sign-out the server already accepted: the session is gone when this returns, and
	// the mark of the intentional sign-out is withdrawn. It shares its lock with the invalidations, so an
	// invalidation that was waiting for it is served by it and tells the user nothing.
	suspend fun endSignedOutSession(sessionId: String)
}
