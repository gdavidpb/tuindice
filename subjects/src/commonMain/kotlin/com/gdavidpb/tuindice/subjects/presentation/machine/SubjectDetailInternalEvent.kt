package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem

/**
 * Internal machine inputs for the subject detail screen. Load and refresh keep separate
 * start/failure events because their reductions differ: the load path replaces any state,
 * while the refresh path preserves Content and Unavailable.
 */
sealed interface SubjectDetailInternalEvent {
	data object DetailLoadStarted : SubjectDetailInternalEvent

	data object DetailRefreshStarted : SubjectDetailInternalEvent

	data class DetailContentLoaded(
		val detail: SubjectDetailItem
	) : SubjectDetailInternalEvent

	data class DetailUnavailableLoaded(
		val subjectCode: String
	) : SubjectDetailInternalEvent

	data class DetailLoadFailed(
		val subjectCode: String
	) : SubjectDetailInternalEvent

	data class DetailRefreshFailed(
		val subjectCode: String
	) : SubjectDetailInternalEvent
}
