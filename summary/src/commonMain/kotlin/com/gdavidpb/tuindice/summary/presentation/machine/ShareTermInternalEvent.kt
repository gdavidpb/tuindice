package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource

/**
 * Internal machine inputs: what the terms say now, or that they cannot be read, and what became of the image that was
 * handed to the platform's share sheet.
 */
sealed interface ShareTermInternalEvent {
	data class TermsObserved(
		val source: ShareTermSource
	) : ShareTermInternalEvent

	data object TermsUnavailable : ShareTermInternalEvent

	data object ImageShared : ShareTermInternalEvent

	data object ImageShareFailed : ShareTermInternalEvent
}
