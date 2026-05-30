package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError

sealed interface RecordUseCaseError : UseCaseError {
	data object Unauthorized : RecordUseCaseError
	data object Timeout : RecordUseCaseError
	data object Unavailable : RecordUseCaseError
	data object NoConnection : RecordUseCaseError
	data class SyntheticTermValidation(
		val reason: SyntheticTermValidationError
	) : RecordUseCaseError
}
