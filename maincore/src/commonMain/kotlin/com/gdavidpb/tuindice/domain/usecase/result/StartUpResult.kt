package com.gdavidpb.tuindice.domain.usecase.result

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.domain.model.StartUpTarget

sealed interface StartUpResult {
	data class Available(
		val startTarget: StartUpTarget
	) : StartUpResult

	data class AppUnavailable(
		val notice: AppAvailabilityNotice
	) : StartUpResult

	data class OutdatedApp(
		val state: OutdatedAppState
	) : StartUpResult
}
