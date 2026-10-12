package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.presentation.contract.PendingSessionEnd

// What the interface owes the user once the session ended behind their back: the way to the sign-in,
// always, and the notice that explains it unless the user was already offered that very notice (its
// stamp). Memory of the machine, not of the screen: it outlives a route that is recomposed or rotated.
internal class SessionEndTracker {
	// The stamp of the last notice put in front of the interface in this process. The start-up offers
	// the notice an invalidation left and the live signal of that same invalidation reads it again: the
	// stamp tells them apart from a later invalidation, whose notice is new, so one notice is shown once
	// however many ways it arrives by.
	private var lastOfferedNoticeStamp: Long? = null

	// The stamp of the notice that goes with the pending end of the session, to take it once attended.
	private var pendingNoticeStamp: Long? = null

	private var lastPendingId = 0L

	// An end that is still pending keeps its notice: neither lost nor duplicated. One that has none takes
	// the notice if this signal brings it, as a new end, so the route that is attending the old one
	// starts again with the message.
	fun begin(current: PendingSessionEnd?, notice: SessionNoticeText?): PendingSessionEnd {
		val offered = notice?.takeIf { candidate -> candidate.stamp != lastOfferedNoticeStamp }

		if (current != null && (current.message != null || offered == null)) return current

		lastPendingId += 1

		if (offered != null) {
			lastOfferedNoticeStamp = offered.stamp
			pendingNoticeStamp = offered.stamp
		}

		return PendingSessionEnd(id = lastPendingId, message = offered?.message)
	}

	// The stamp of the notice to take now that [pending] was attended, or null when it had none.
	fun finish(pending: PendingSessionEnd): Long? {
		val stamp = pendingNoticeStamp.takeIf { pending.message != null } ?: return null

		pendingNoticeStamp = null

		return stamp
	}
}
