package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// The consent writes of Preferences reach the store one after the other, in the order the student moved the switch: a
// write starts once the one before it has ended, whatever its outcome. So the last move is the last one the store
// receives, and the device keeps that same one.
//
// The link is taken when the move is made, in the transition of the screen, and not when the write starts: the writes
// start in the session, on whichever thread is free, so their order cannot be left to them. The writes run in the
// session as well, so leaving Preferences does not cut a write short.
//
// The queue is shared by every Preferences screen, so it says whether a write is pending: the screens show the store's
// value only once no write is, and then any screen, even one opened during a write, takes the store's value.
class AssistantConsentWriteQueue(
	private val sessionCoroutineScope: SessionCoroutineScope
) {
	// Orders the enqueueing: each write is chained behind the last one queued, and this lock keeps two enqueues from
	// chaining behind the same write.
	private val mutex = Mutex()

	// The last write queued: the next one waits for it.
	private var lastWrite: Job? = null

	// How many writes are queued or running. A write leaves the count when it ends, whatever its outcome, also when the
	// session cancels it. So a write cancelled while it waits for an older one leaves at once, and the older one still
	// counts until it ends. The count is the one source of the flag below, so concurrent ends cannot leave them apart.
	private val writesInFlight = MutableStateFlow(0)

	// How many writes have ended, whatever their outcome. It only grows, so each end is a new value: a reader that
	// follows it sees the last end even when a write starts and ends between two of its reads, which a flag going
	// from false to true and back would hide.
	private val endedWrites = MutableStateFlow(0L)

	// Counts up each time a write ends, after it has left the in-flight count.
	val writeEnds: StateFlow<Long> = endedWrites.asStateFlow()

	// Whether a write is queued or running now.
	val hasPendingWrites: Boolean
		get() = writesInFlight.value > 0

	// Queues [write] behind the write before it and returns at once, so the order is set before the caller goes on.
	suspend fun enqueue(write: suspend () -> Unit): Job {
		return mutex.withLock {
			val previous = lastWrite

			val current = sessionCoroutineScope.launch {
				// Joining a job waits for its end, and does not rethrow its failure.
				previous?.join()

				write()
			}

			lastWrite = current

			// The count goes up before the completion handler is registered, so the handler can never take it below zero.
			writesInFlight.update { inFlight -> inFlight + 1 }

			// The handler takes no lock: it only leaves the count, so it works whether the write ended normally or was
			// cancelled by the session.
			current.invokeOnCompletion {
				writesInFlight.update { inFlight -> inFlight - 1 }
				endedWrites.update { ended -> ended + 1 }
			}

			current
		}
	}
}
