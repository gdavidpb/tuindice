package com.gdavidpb.tuindice.base.domain.model

/**
 * A request to show an attempt of the Record tab: the attempt to bring into view, in the term that holds
 * it. Two requests are the same only when they name the same attempt, so the consumer takes exactly the
 * request it was shown and no other.
 */
data class RecordAttemptFocusRequest(
	val attemptId: String
)
