package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress

/**
 * What Summary says beyond the profile, for the moment it was read: what needs attention, the
 * current week, the current term and the career. [attention], [week] and [term] are absent (empty
 * and null) when there is no current term to speak of; [career] is null when no pensum is cached.
 */
data class SummaryHub(
	val attention: List<SummaryAttention>,
	val week: SummaryWeek?,
	val term: SummaryTerm?,
	val career: PensumCreditsProgress?
) {
	companion object {
		/** Nothing to say about the term, and no pensum. */
		val Empty = SummaryHub(attention = emptyList(), week = null, term = null, career = null)
	}
}
