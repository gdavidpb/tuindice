package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

/**
 * Internal machine inputs for the app host: the startup lifecycle split per outcome,
 * the version refusals observed afterwards, plus the platform flow triggers (review, update).
 */
sealed interface MainInternalEvent {
	data object StartUpStarting : MainInternalEvent

	data class StartUpCompleted(
		val startDestination: Destination,
		val sessionResetMessage: String? = null
	) : MainInternalEvent

	data class AppUnavailableResolved(
		val notice: AppAvailabilityNotice
	) : MainInternalEvent

	data class OutdatedAppResolved(
		val outdatedAppState: OutdatedAppState
	) : MainInternalEvent

	/** A request made while the app was running was refused for this version. */
	data class OutdatedAppObserved(
		val outdatedAppState: OutdatedAppState
	) : MainInternalEvent

	data class StartUpFailed(
		val noServices: Boolean
	) : MainInternalEvent

	data object ReviewRequested : MainInternalEvent

	data class UpdateInfoLoaded(
		val action: UpdateAction
	) : MainInternalEvent

	/** How the last sync ended, observed while the content is up. */
	data class SyncStatusObserved(
		val syncStatus: SyncStatus
	) : MainInternalEvent

	/** The server ended the session behind the user's back. */
	data class SessionInvalidationObserved(
		val message: String
	) : MainInternalEvent

	/** The pending work was read: the sign-out dialog can say what would be lost. */
	data class SignOutPrepared(
		val pendingChanges: PendingChanges
	) : MainInternalEvent

	data class SignOutPreparationFailed(
		val message: String
	) : MainInternalEvent
}
