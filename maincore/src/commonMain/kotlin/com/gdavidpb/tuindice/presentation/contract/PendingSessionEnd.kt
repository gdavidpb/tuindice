package com.gdavidpb.tuindice.presentation.contract

/**
 * The session ended behind the user's back and the interface owes them the way to the sign-in. It is
 * state, not an event: it stays in [Main.State] until the interface says it attended it, so a route
 * that was cancelled while it waited (a rotation, a recomposition) attends it again when it is back.
 * [id] is local to the machine and tells one pending end from the next; [message] is the notice that
 * goes with it, or null when there is nothing to tell (the user was already told).
 */
data class PendingSessionEnd(
	val id: Long,
	val message: String?
)
