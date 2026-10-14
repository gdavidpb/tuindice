package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress

/**
 * What Summary says beyond the profile, for the moment it was read: what needs attention, the week, the term and the
 * career. [week] and [term] are null only when the record is not ready for any reason but a new student the university
 * has no record for yet (it is unavailable, denied, failed or not read yet): then only the career remains. Otherwise
 * both are there, each saying what it has or inviting to what it lacks. [career] is null when no pensum is cached.
 */
data class SummaryHub(
	val attention: List<SummaryAttention>,
	val week: SummaryWeek?,
	val term: SummaryTerm?,
	val career: PensumCreditsProgress?
) {
	companion object {
		/** Nothing to say about the week or the term, and no pensum. */
		val Empty = SummaryHub(attention = emptyList(), week = null, term = null, career = null)
	}
}
