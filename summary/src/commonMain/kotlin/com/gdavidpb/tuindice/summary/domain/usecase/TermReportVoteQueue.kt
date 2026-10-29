package com.gdavidpb.tuindice.summary.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// The votes of a report reach the service one after the other, in the order the student gave them: a vote starts once
// the one before it, for the same report, has ended, whatever its outcome. So the last thumb is the last one the
// service receives, and the device keeps that same one.
//
// There is one queue for the whole app: a screen that is left and opened again while a vote is on its way queues
// behind that vote, as the screen that sent it would.
class TermReportVoteQueue {
	private val mutex = Mutex()

	// The last vote started for each report: the next vote of that report waits for it. A report's entry goes when its
	// last vote ends, so the map holds only the reports that still have a vote in the queue.
	private val lastVotes = MutableStateFlow<Map<String, Deferred<Unit>>>(emptyMap())

	// Starts [write] in [scope] once the vote before it, for the same report, has ended. The deferred completes when
	// [write] does. The outcome of the vote before does not decide this one: a vote that failed stops nothing after it.
	suspend fun enqueue(scope: CoroutineScope, insightId: String, write: suspend () -> Unit): Deferred<Unit> {
		return mutex.withLock {
			val previous = lastVotes.value[insightId]

			val current = scope.async {
				// Joining a deferred waits for its end, and does not rethrow its failure.
				previous?.join()

				write()
			}

			lastVotes.update { votes -> votes + (insightId to current) }

			// However the vote ends, it leaves the map, unless a newer vote of its report has taken its place since.
			current.invokeOnCompletion {
				lastVotes.update { votes -> if (votes[insightId] === current) votes - insightId else votes }
			}

			current
		}
	}

	// Only for the tests: how many reports still have a vote tracked.
	internal fun trackedReportCount(): Int = lastVotes.value.size
}
