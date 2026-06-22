package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

/**
 * Internal machine inputs for the app host: the startup lifecycle split per outcome,
 * plus the platform flow triggers (review, update).
 */
sealed interface MainInternalEvent {
	data object StartUpStarting : MainInternalEvent

	data class StartUpCompleted(
		val startDestination: Destination
	) : MainInternalEvent

	data class AppUnavailableResolved(
		val notice: AppAvailabilityNotice
	) : MainInternalEvent

	data class OutdatedAppResolved(
		val outdatedAppState: OutdatedAppState
	) : MainInternalEvent

	data class StartUpFailed(
		val noServices: Boolean
	) : MainInternalEvent

	data object ReviewRequested : MainInternalEvent

	data class UpdateInfoLoaded(
		val action: UpdateAction
	) : MainInternalEvent
}
