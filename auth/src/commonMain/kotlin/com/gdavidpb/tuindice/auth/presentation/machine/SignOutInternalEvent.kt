package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.PendingChanges

/**
 * Internal machine inputs for sign-out: initialization split per outcome, the
 * logging-out lifecycle shared by confirm/flush/force, and the terminal results.
 */
sealed interface SignOutInternalEvent {
	data object SignOutInitializedPlain : SignOutInternalEvent

	data class SignOutInitializedPending(
		val pendingChanges: PendingChanges
	) : SignOutInternalEvent

	data class LoggingOutObserved(
		val pendingChanges: PendingChanges?,
		val requiresPasswordUpdate: Boolean
	) : SignOutInternalEvent

	data class PendingChangesFound(
		val pendingChanges: PendingChanges
	) : SignOutInternalEvent

	data object SignOutSucceeded : SignOutInternalEvent

	data class SignOutFailedToPlain(
		val message: String
	) : SignOutInternalEvent

	data class FlushFailedObserved(
		val pendingChanges: PendingChanges,
		val requiresPasswordUpdate: Boolean,
		val message: String?
	) : SignOutInternalEvent
}
