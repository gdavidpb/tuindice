@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal object SyntheticTermCommandValidator {
	fun validate(
		record: AcademicRecord,
		params: CreateSyntheticTermParams
	) {
		val editingTerm = params.editingTermId?.let { editingTermId ->
			record.terms.firstOrNull { term -> term.id == editingTermId && term.kind.isSynthetic }
				?: throw SyntheticTermValidationException(SyntheticTermValidationError.TERM_NOT_FOUND)
		}
		val otherTerms = if (editingTerm == null) {
			record.terms
		} else {
			record.terms.filterNot { term -> term.id == editingTerm.id }
		}
		val keepsSameTermKey = editingTerm?.termKey == params.period.termKey

		validatePeriod(
			otherTerms = otherTerms,
			period = params.period,
			keepsSameTermKey = keepsSameTermKey
		)
		validateSubjects(
			otherTerms = otherTerms,
			subjects = params.subjects
		)
	}

	private fun validatePeriod(
		otherTerms: List<AcademicTerm>,
		period: SyntheticTermPeriodOption,
		keepsSameTermKey: Boolean
	) {
		if (!period.periodCode.supportsSyntheticPlanning) {
			throw SyntheticTermValidationException(SyntheticTermValidationError.UNSUPPORTED_PERIOD)
		}
		if (otherTerms.any { term -> term.termKey == period.termKey }) {
			throw SyntheticTermValidationException(SyntheticTermValidationError.TERM_ALREADY_EXISTS)
		}
		if (!keepsSameTermKey && period.termOrder < currentAcademicTermOrder()) {
			throw SyntheticTermValidationException(SyntheticTermValidationError.PERIOD_IN_PAST)
		}
		if (!keepsSameTermKey && otherTerms.maxOfOrNull(AcademicTerm::termOrder)?.let { latestOrder ->
				period.termOrder <= latestOrder
			} == true
		) {
			throw SyntheticTermValidationException(SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST)
		}
	}

	private fun validateSubjects(
		otherTerms: List<AcademicTerm>,
		subjects: List<SyntheticTermSubject>
	) {
		val subjectCodes = subjects.map { subject -> subject.subjectCode.trim().uppercase() }
		if (subjectCodes.distinct().size != subjectCodes.size) {
			throw SyntheticTermValidationException(SyntheticTermValidationError.DUPLICATE_SUBJECT)
		}

		val plannedSubjectCodes = otherTerms
			.filter { term -> term.kind.isSynthetic }
			.flatMap(AcademicTerm::attempts)
			.map { attempt -> attempt.subjectCode.uppercase() }
			.toSet()
		val approvedSubjectCodes = otherTerms
			.filter { term -> term.kind.isHistorical || term.kind.isCurrent }
			.flatMap { term ->
				term.attempts.filter { attempt ->
					attempt.academicOutcome == AttemptOutcome.APPROVED
				}
			}
			.map { attempt -> attempt.subjectCode.uppercase() }
			.toSet()

		when {
			subjectCodes.any { subjectCode -> subjectCode in approvedSubjectCodes } ->
				throw SyntheticTermValidationException(SyntheticTermValidationError.SUBJECT_ALREADY_APPROVED)
			subjectCodes.any { subjectCode -> subjectCode in plannedSubjectCodes } ->
				throw SyntheticTermValidationException(SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED)
		}
	}

	private fun currentAcademicTermOrder(): Int {
		val dateTime = Clock.System.now().toLocalDateTime(TimeZone.of(AcademicCalendarTimeZoneId))
		return dateTime.year * 10 + periodForMonth(dateTime.month.ordinal + 1).sequence
	}

	private fun periodForMonth(month: Int): AcademicTermPeriod {
		return when (month) {
			in 1..3 -> AcademicTermPeriod.JAN_MAR
			in 4..6 -> AcademicTermPeriod.APR_JUL
			in 7..8 -> AcademicTermPeriod.JUL_AUG
			else -> AcademicTermPeriod.SEP_DEC
		}
	}
}

private const val AcademicCalendarTimeZoneId = "America/Caracas"
