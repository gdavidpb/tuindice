package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface RecordUseCaseError : UseCaseError {
	data object Unauthorized : RecordUseCaseError
	data object Timeout : RecordUseCaseError
	data object Unavailable : RecordUseCaseError
	data object NoConnection : RecordUseCaseError
}
