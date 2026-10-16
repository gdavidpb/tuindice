package com.gdavidpb.tuindice.domain.model

/**
 * Why the session was ended, reported with the invalidation so a logout that nobody asked for
 * leaves its cause behind.
 */
enum class SessionInvalidationReason {
	RefreshRejected,
	InsufficientScope
}
