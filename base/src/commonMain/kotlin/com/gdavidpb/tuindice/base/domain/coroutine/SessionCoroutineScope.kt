package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.CoroutineContext

private const val MAX_CANCELLATION_PASSES = 8

// How long one pass waits for the children it cancelled. A child that cooperates ends in
// milliseconds (a cleanup, a local write); one that does not (a request inside a context that
// cannot be cancelled) ends with the HTTP timeout, 90 seconds by default. The bound sits well under
// that, so the sign-out does not stay on its spinner for a request nobody is waiting for, and well
// above any cooperative cancellation, so a child that is merely slow is still waited for.
private const val CANCELLATION_PASS_TIMEOUT_MILLIS = 10_000L

class SessionCoroutineScope(
	dispatchers: TuIndiceDispatchers,
	private val reportingRepository: ReportingRepository? = null
) : CoroutineScope {
	private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

	override val coroutineContext: CoroutineContext
		get() = scope.coroutineContext

	// Cancels the scope's children and waits for them, until none is left: a child that is launched
	// while the others are cancelling (a cleanup that starts more work, a sync that reaches its last
	// step) would otherwise outlive the session. The caller's own branch is left alone: a request
	// that runs inside the scope (a sync, a revalidation) can be the one that ends the session, and
	// cancelling it would make this call throw before the session is wiped, or wait for itself.
	//
	// Returns that branch (the child of the scope the caller runs in, null when it runs outside),
	// so whoever ends the session can cancel it as its very last step, once everything is wiped:
	// the request then unwinds as a cancellation instead of writing its failure into the empty stores.
	//
	// The number of passes is bounded: a child that relaunches work every time it is cancelled would
	// otherwise keep the sign-out spinning forever. So is the wait of each pass: a child that ignores
	// its cancellation would hold the sign-out for as long as it runs. Past either bound the leftover
	// work is reported and left running; ending the session matters more than waiting for it. What it
	// writes afterwards is fenced by the session epoch of each holder.
	suspend fun cancelActiveWork(): Job? {
		val caller = currentCoroutineContext()[Job]
		var callerBranch: Job? = null
		var settled = false
		var pass = 0

		while (!settled && pass < MAX_CANCELLATION_PASSES) {
			pass += 1

			val children = coroutineContext[Job]?.children?.toList().orEmpty()
			val (own, pending) = children.partition { child ->
				caller != null && (child === caller || child.isAncestorOf(caller))
			}

			callerBranch = own.firstOrNull() ?: callerBranch

			if (pending.isEmpty()) {
				settled = true
			} else {
				pending.forEach { child -> child.cancel() }

				if (withTimeoutOrNull(CANCELLATION_PASS_TIMEOUT_MILLIS) { pending.joinAll() } == null) {
					reportingRepository?.logException(
						IllegalStateException(
							"The session scope still had ${pending.count { child -> !child.isCompleted }} child(ren) " +
								"after waiting ${CANCELLATION_PASS_TIMEOUT_MILLIS}ms for their cancellation"
						)
					)
					settled = true
				}
			}
		}

		if (!settled) {
			reportingRepository?.logException(
				IllegalStateException(
					"The session scope still had work after $MAX_CANCELLATION_PASSES cancellation passes"
				)
			)
		}

		return callerBranch
	}

	// The child of the scope the caller runs in, or null when it runs outside of it.
	suspend fun callerBranch(): Job? {
		val caller = currentCoroutineContext()[Job] ?: return null

		return coroutineContext[Job]?.children?.firstOrNull { child ->
			child === caller || child.isAncestorOf(caller)
		}
	}

	private fun Job.isAncestorOf(job: Job): Boolean {
		return children.any { child -> child === job || child.isAncestorOf(job) }
	}
}
