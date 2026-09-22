package com.gdavidpb.tuindice.subjects.data.model

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumSlotEligibilityResolver
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotEligibility
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotKind
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus

/**
 * Labels a search result against the student's pensum, resolved once per pensum and record so a
 * search can label every result without resolving the pensum again. Fixed courses (and their
 * equivalence codes, and codes that already fulfilled a node) are looked up; anything else is
 * asked to the slot resolver, and only a slot still open earns a badge.
 */
class SubjectSearchPensumStatusResolver(
	private val statusBySubjectCode: Map<String, SubjectSearchPensumStatus>,
	private val slotEligibility: AcademicPensumSlotEligibilityResolver?
) {
	fun statusOf(subjectCode: String, credits: Int): SubjectSearchPensumStatus? {
		val code = subjectCode.trim().uppercase()

		return statusBySubjectCode[code]
			?: slotEligibility?.eligibilityOf(subjectCode = code, credits = credits).toSlotStatus()
	}

	private fun AcademicPensumSlotEligibility?.toSlotStatus(): SubjectSearchPensumStatus? {
		return when (this) {
			is AcademicPensumSlotEligibility.CountsTowardSlot -> when (slotKind) {
				AcademicPensumSlotKind.ELECTIVE -> SubjectSearchPensumStatus.COUNTS_AS_ELECTIVE
				AcademicPensumSlotKind.GENERAL_STUDIES -> SubjectSearchPensumStatus.COUNTS_AS_GENERAL_STUDIES
			}

			is AcademicPensumSlotEligibility.SlotsFilled,
			AcademicPensumSlotEligibility.NotEligible,
			null,
			-> null
		}
	}

	companion object {
		val Empty = SubjectSearchPensumStatusResolver(statusBySubjectCode = emptyMap(), slotEligibility = null)
	}
}
