@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.fixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SyntheticTermCommandValidatorTest {
	private fun validate(record: AcademicRecord, params: CreateSyntheticTermParams, clock: Clock = Clock.System) {
		SyntheticTermCommandValidator.validate(record = record, params = params, clock = clock)
	}

	@Test
	fun validate_passes_whenCreatingFutureTermWithUntakenSubjects() {
		val record = record(
			academicTerm(
				id = "historical",
				periodYear = 2024,
				attempts = listOf(
					academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.FAILED),
					academicAttempt(subjectCode = "FS1111", outcome = AttemptOutcome.RETIRED)
				)
			)
		)

		validate(
			record = record,
			params = creationParams(
				subjects = listOf(
					syntheticSubject("MA1112"),
					syntheticSubject("FS1111")
				)
			)
		)
	}

	@Test
	fun validate_throwsTermAlreadyExists_whenAnotherTermUsesSamePeriod() {
		val record = record(
			academicTerm(
				id = "syn-existing",
				kind = TermKind.SYNTHETIC,
				periodYear = 9999,
				periodCode = AcademicTermPeriod.SEP_DEC
			)
		)

		assertValidationError(SyntheticTermValidationError.TERM_ALREADY_EXISTS) {
			validate(
				record = record,
				params = creationParams(
					period = SyntheticTermPeriodOption(
						periodYear = 9999,
						periodCode = AcademicTermPeriod.SEP_DEC
					)
				)
			)
		}
	}

	@Test
	fun validate_throwsUnsupportedPeriod_whenCreatingLongAcademicTerm() {
		assertValidationError(SyntheticTermValidationError.UNSUPPORTED_PERIOD) {
			validate(
				record = record(),
				params = creationParams(
					period = SyntheticTermPeriodOption(
						periodYear = 9999,
						periodCode = AcademicTermPeriod.JAN_MAY
					)
				)
			)
		}
	}

	@Test
	fun validate_throwsPeriodInPast_whenNewTermKeyIsBeforeCurrentPeriod() {
		assertValidationError(SyntheticTermValidationError.PERIOD_IN_PAST) {
			validate(
				record = record(),
				params = creationParams(
					period = SyntheticTermPeriodOption(
						periodYear = 2000,
						periodCode = AcademicTermPeriod.JAN_MAR
					)
				)
			)
		}
	}

	// The check reads the clock it is given: the same period is valid in one term and in the past after it.
	@Test
	fun validate_readsTheClockItIsGiven_whenCheckingThatThePeriodIsNotInThePast() {
		val september2026 = SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC
		)

		validate(
			record = record(),
			params = creationParams(period = september2026),
			clock = fixedClock("2026-10-15T12:00:00Z")
		)
		assertValidationError(SyntheticTermValidationError.PERIOD_IN_PAST) {
			validate(
				record = record(),
				params = creationParams(period = september2026),
				clock = fixedClock("2027-01-10T12:00:00Z")
			)
		}
	}

	@Test
	fun validate_readsTheUniversityTimeZone_whenTheInstantIsPastMidnightInUtc() {
		val december2026 = SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC
		)

		// 2027-01-01T01:00Z is still 2026-12-31 in Caracas (UTC-4).
		validate(
			record = record(),
			params = creationParams(period = december2026),
			clock = fixedClock("2027-01-01T01:00:00Z")
		)
	}

	@Test
	fun validate_throwsTermMustBeAfterLatest_whenNewTermKeyIsNotAfterLatestTerm() {
		val record = record(
			academicTerm(
				id = "syn-latest",
				kind = TermKind.SYNTHETIC,
				periodYear = 9999,
				periodCode = AcademicTermPeriod.SEP_DEC
			)
		)

		assertValidationError(SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST) {
			validate(
				record = record,
				params = creationParams(
					period = SyntheticTermPeriodOption(
						periodYear = 9999,
						periodCode = AcademicTermPeriod.JUL_AUG
					)
				)
			)
		}
	}

	@Test
	fun validate_throwsTermNotFound_whenEditingTermIsMissingOrNotSynthetic() {
		val record = record(academicTerm(id = "historical", periodYear = 2024))

		assertValidationError(SyntheticTermValidationError.TERM_NOT_FOUND) {
			validate(
				record = record,
				params = creationParams(
					editingTermId = "historical",
					editingTermKey = "2024-JAN_MAR"
				)
			)
		}
	}

	@Test
	fun validate_allowsKeepingOwnPastPeriod_whenEditingTerm() {
		val record = record(
			academicTerm(
				id = "syn-1",
				kind = TermKind.SYNTHETIC,
				periodYear = 2020,
				periodCode = AcademicTermPeriod.JAN_MAR
			),
			academicTerm(id = "historical", periodYear = 2024)
		)

		validate(
			record = record,
			params = creationParams(
				editingTermId = "syn-1",
				editingTermKey = "2020-JAN_MAR",
				period = SyntheticTermPeriodOption(
					periodYear = 2020,
					periodCode = AcademicTermPeriod.JAN_MAR
				)
			)
		)
	}

	@Test
	fun validate_allowsResubmittingOwnSubjects_whenEditingTerm() {
		val record = record(
			academicTerm(
				id = "syn-1",
				kind = TermKind.SYNTHETIC,
				periodYear = 9999,
				periodCode = AcademicTermPeriod.JAN_MAR,
				attempts = listOf(
					academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.PENDING)
				)
			)
		)

		validate(
			record = record,
			params = creationParams(
				editingTermId = "syn-1",
				editingTermKey = "9999-JAN_MAR",
				period = SyntheticTermPeriodOption(
					periodYear = 9999,
					periodCode = AcademicTermPeriod.JAN_MAR
				),
				subjects = listOf(syntheticSubject("MA1112"))
			)
		)
	}

	@Test
	fun validate_throwsDuplicateSubject_normalizingCaseAndWhitespace() {
		assertValidationError(SyntheticTermValidationError.DUPLICATE_SUBJECT) {
			validate(
				record = record(),
				params = creationParams(
					subjects = listOf(
						syntheticSubject("ma1112"),
						syntheticSubject(" MA1112 ")
					)
				)
			)
		}
	}

	@Test
	fun validate_throwsSubjectAlreadyApproved_whenSubjectIsApprovedInCurrentTerm() {
		val record = record(
			academicTerm(
				id = "current",
				kind = TermKind.CURRENT,
				periodYear = 2024,
				attempts = listOf(
					academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		assertValidationError(SyntheticTermValidationError.SUBJECT_ALREADY_APPROVED) {
			validate(
				record = record,
				params = creationParams(subjects = listOf(syntheticSubject("ma1112")))
			)
		}
	}

	@Test
	fun validate_throwsSubjectAlreadyPlanned_whenSubjectExistsInAnotherSyntheticTerm() {
		val record = record(
			academicTerm(
				id = "syn-other",
				kind = TermKind.SYNTHETIC,
				periodYear = 9998,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.PENDING)
				)
			)
		)

		assertValidationError(SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED) {
			validate(
				record = record,
				params = creationParams(subjects = listOf(syntheticSubject("MA1112")))
			)
		}
	}

	private fun assertValidationError(
		expected: SyntheticTermValidationError,
		block: () -> Unit
	) {
		val exception = assertFailsWith<SyntheticTermValidationException>(block = block)
		assertEquals(expected, exception.reason)
	}

	private fun record(vararg terms: AcademicTerm): AcademicRecord {
		return AcademicRecord(
			id = "record",
			terms = terms.toList()
		)
	}

	private fun creationParams(
		editingTermId: String? = null,
		editingTermKey: String? = null,
		period: SyntheticTermPeriodOption = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		),
		subjects: List<SyntheticTermSubject> = listOf(syntheticSubject("MA1112"))
	): CreateSyntheticTermParams {
		return CreateSyntheticTermParams(
			editingTermId = editingTermId,
			editingTermKey = editingTermKey,
			period = period,
			subjects = subjects
		)
	}

	private fun syntheticSubject(subjectCode: String): SyntheticTermSubject {
		return SyntheticTermSubject(
			subjectCode = subjectCode,
			name = subjectCode,
			credits = 4
		)
	}
}
