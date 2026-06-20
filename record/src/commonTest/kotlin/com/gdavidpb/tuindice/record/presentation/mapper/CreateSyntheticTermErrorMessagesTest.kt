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
import tuindice.record.generated.resources.create_term_error_unsupported_period
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateSyntheticTermErrorMessagesTest {
	@Test
	fun toSubmitErrorText_mapsEveryValidationReason_toItsMessage() {
		val expectations = mapOf(
			SyntheticTermValidationError.RECORD_UNAVAILABLE to Res.string.create_term_error_record_unavailable,
			SyntheticTermValidationError.TERM_NOT_FOUND to Res.string.create_term_error_record_unavailable,
			SyntheticTermValidationError.UNSUPPORTED_PERIOD to Res.string.create_term_error_unsupported_period,
			SyntheticTermValidationError.PERIOD_IN_PAST to Res.string.create_term_error_period_in_past,
			SyntheticTermValidationError.TERM_ALREADY_EXISTS to Res.string.create_term_error_term_already_exists,
			SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST to Res.string.create_term_error_term_must_be_after_latest,
			SyntheticTermValidationError.DUPLICATE_SUBJECT to Res.string.create_term_error_duplicate_subject,
			SyntheticTermValidationError.SUBJECT_ALREADY_TAKEN to Res.string.create_term_error_subject_already_taken,
			SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED to Res.string.create_term_error_subject_already_planned
		)

		assertEquals(SyntheticTermValidationError.entries.toSet(), expectations.keys)

		expectations.forEach { (reason, resource) ->
			assertEquals(
				UiText.Resource(resource),
				RecordUseCaseError.SyntheticTermValidation(reason).toSubmitErrorText(),
				"reason: $reason"
			)
		}
	}

	@Test
	fun toSubmitErrorText_fallsBackToGenericMessage_forTransportErrorsAndNull() {
		val errors: List<RecordUseCaseError?> = listOf(
			RecordUseCaseError.NoConnection,
			RecordUseCaseError.Timeout,
			RecordUseCaseError.Unauthorized,
			RecordUseCaseError.Unavailable,
			null
		)

		errors.forEach { error ->
			assertEquals(
				UiText.Resource(Res.string.create_term_error_generic),
				error.toSubmitErrorText(),
				"error: $error"
			)
		}
	}
}
