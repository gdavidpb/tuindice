package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

// One step of a job that must go on to its end: a step that fails is skipped, and its failure is what
// this returns (null if it succeeded). A CancellationException is not always the cancellation of the
// job (a timeout inside the step, a child it waited for): it is one more failure of the step, and only a
// job that is no longer active is stopped by it.
suspend inline fun runStep(block: () -> Unit): Throwable? {
	return runCatching(block).onFailure { failure ->
		if (failure is CancellationException) currentCoroutineContext().ensureActive()
	}.exceptionOrNull()
}
