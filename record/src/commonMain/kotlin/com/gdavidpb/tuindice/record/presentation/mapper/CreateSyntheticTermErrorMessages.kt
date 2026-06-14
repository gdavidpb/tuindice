package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_error_duplicate_subject
import tuindice.record.generated.resources.create_term_error_generic
import tuindice.record.generated.resources.create_term_error_period_in_past
import tuindice.record.generated.resources.create_term_error_record_unavailable
import tuindice.record.generated.resources.create_term_error_subject_already_planned
import tuindice.record.generated.resources.create_term_error_subject_already_taken
import tuindice.record.generated.resources.create_term_error_term_already_exists
import tuindice.record.generated.resources.create_term_error_term_must_be_after_latest

internal fun RecordUseCaseError?.toSubmitErrorText(): UiText {
	return when (this) {
		is RecordUseCaseError.SyntheticTermValidation -> UiText.Resource(
			when (reason) {
				SyntheticTermValidationError.RECORD_UNAVAILABLE,
				SyntheticTermValidationError.TERM_NOT_FOUND,
				-> Res.string.create_term_error_record_unavailable

				SyntheticTermValidationError.PERIOD_IN_PAST -> Res.string.create_term_error_period_in_past
				SyntheticTermValidationError.TERM_ALREADY_EXISTS -> Res.string.create_term_error_term_already_exists
				SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST -> Res.string.create_term_error_term_must_be_after_latest
				SyntheticTermValidationError.DUPLICATE_SUBJECT -> Res.string.create_term_error_duplicate_subject
				SyntheticTermValidationError.SUBJECT_ALREADY_TAKEN -> Res.string.create_term_error_subject_already_taken
				SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED -> Res.string.create_term_error_subject_already_planned
			}
		)

		RecordUseCaseError.NoConnection,
		RecordUseCaseError.Timeout,
		RecordUseCaseError.Unauthorized,
		RecordUseCaseError.Unavailable,
		null,
		-> UiText.Resource(Res.string.create_term_error_generic)
	}
}
