package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic

/**
 * What the share sheet of a closed term reads, for the moment it was read: the [closedTerms] of the statistics in
 * chronological order (the ones without an index are left to the sheet, which has no card for them), the [careerName]
 * of the student, the [backgrounds] a card can wear and the one the student chose last ([preferredBackgroundId]).
 */
data class ShareTermSource(
	val closedTerms: List<TermStatistic>,
	val careerName: String,
	val backgrounds: List<ShareBackground>,
	val preferredBackgroundId: String?
)
