package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.RecordAttemptFocusRequest
import kotlinx.coroutines.flow.StateFlow

/**
 * A request, kept as state, to show an attempt of the Record tab: whoever asks (the host, when a screen
 * sends the user there) writes it, and the Record machine applies it once its content is ready and
 * consumes it. It is state and not an event so it survives the tab not being composed yet, or its
 * content not having loaded, at the moment of the request.
 */
interface RecordAttemptFocusRepository {
	/** The request not yet consumed, or null when nothing is pending. */
	val pending: StateFlow<RecordAttemptFocusRequest?>

	/** Asks for [attemptId] to be shown; a request not yet consumed is replaced. */
	fun request(attemptId: String)

	/**
	 * Takes the pending request when it is still [request]. True for the caller that took it, so a
	 * request is applied once however many times it is seen; false when it was already taken or a
	 * newer one replaced it.
	 */
	fun consume(request: RecordAttemptFocusRequest): Boolean
}
