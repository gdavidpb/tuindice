package com.gdavidpb.tuindice.domain.usecase.result

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.domain.model.StartUpTarget

sealed interface StartUpResult {
	data class Available(
		val startTarget: StartUpTarget,
		// The notice owed to the user about the session that was reset, and why; null when none is owed.
		val sessionResetNotice: SessionResetNoticeReason? = null
	) : StartUpResult

	data class AppUnavailable(
		val notice: AppAvailabilityNotice
	) : StartUpResult

	data class OutdatedApp(
		val state: OutdatedAppState
	) : StartUpResult
}
