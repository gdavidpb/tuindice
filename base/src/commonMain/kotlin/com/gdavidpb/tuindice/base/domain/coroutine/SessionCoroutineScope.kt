package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.joinAll
import kotlin.coroutines.CoroutineContext

class SessionCoroutineScope(
	dispatchers: TuIndiceDispatchers
) : CoroutineScope {
	private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

	override val coroutineContext: CoroutineContext
		get() = scope.coroutineContext

	// Cancels the scope's children and waits for them, until none is left: a child that is launched
	// while the others are cancelling (a cleanup that starts more work, a sync that reaches its last
	// step) would otherwise outlive the session. The caller's own branch is left alone: a request
	// that runs inside the scope (a sync, a revalidation) can be the one that ends the session, and
	// cancelling it would make this call throw before the session is wiped, or wait for itself.
	suspend fun cancelActiveWork() {
		val caller = currentCoroutineContext()[Job]

		while (true) {
			val pending = coroutineContext[Job]?.children
				?.filterNot { child -> caller != null && (child === caller || child.isAncestorOf(caller)) }
				?.toList()
				.orEmpty()

			if (pending.isEmpty()) return

			pending.forEach { child -> child.cancel() }
			pending.joinAll()
		}
	}

	private fun Job.isAncestorOf(job: Job): Boolean {
		return children.any { child -> child === job || child.isAncestorOf(job) }
	}
}
