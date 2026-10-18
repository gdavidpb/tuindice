package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface RateTermReportUseCaseError : UseCaseError {
	// The service refused the vote: the report is not there for the account, or the session does not allow it.
	data object Refused : RateTermReportUseCaseError

	data object NoConnection : RateTermReportUseCaseError
}
