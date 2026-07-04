package com.gdavidpb.tuindice.pensum.presentation.machine

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError

/**
 * Internal machine inputs: emissions of the long-lived pensum observation and results
 * of the refresh family (refresh + selections share one reducer, so they share events).
 * User inputs are the contract [com.gdavidpb.tuindice.pensum.presentation.contract.Pensum.Action]
 * classes themselves.
 */
sealed interface PensumInternalEvent {
	data class PensumContentObserved(
		val pensum: ObservedPensum,
		val isSummaryCollapsed: Boolean
	) : PensumInternalEvent

	data object PensumDataMissing : PensumInternalEvent

	data object PensumRecordDataUnavailableObserved : PensumInternalEvent

	data object PensumObservationFailed : PensumInternalEvent

	data object PensumRefreshLoading : PensumInternalEvent

	data object PensumRefreshSucceeded : PensumInternalEvent

	data object PensumRefreshNotFound : PensumInternalEvent

	data class PensumRefreshFailed(
		val error: UpdatePensumUseCaseError?
	) : PensumInternalEvent
}
