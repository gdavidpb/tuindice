package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface ObserveTermReportUseCaseError : UseCaseError {
	// The academic record is not synced yet, so the figures of the term cannot be read.
	data object RecordNotReady : ObserveTermReportUseCaseError

	// The service could not be asked for the report and the report is not on this device.
	data object NoConnection : ObserveTermReportUseCaseError
}
