package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CancellationException

// runCatching that does not swallow the cancellation: a step that fails is skipped, a caller that
// is cancelled is not.
inline fun <T> attempt(block: () -> T): Result<T> {
	return runCatching(block).onFailure { failure -> if (failure is CancellationException) throw failure }
}
