package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress

/**
 * What the Statistics screen reads, for the moment it was read. [closed] is what the closed terms of the record say,
 * and [pace] how the regular ones pace the pensum. [bestTerm] and [worstTerm] are the closed terms with the highest
 * and the lowest index, null when none has one. [lastClosedTerm] is the one that closed last, which the screen names
 * as "al cierre de". [pensumProgress] is the other source of credits, the pensum's own, null when none is cached.
 * [currentTerm] is the term in progress, null when there is none.
 */
data class SummaryStatistics(
	val closed: RecordStatistics,
	val pace: PaceStatistics,
	val bestTerm: TermStatistic?,
	val worstTerm: TermStatistic?,
	val lastClosedTerm: TermStatistic?,
	val pensumProgress: PensumCreditsProgress?,
	val currentTerm: SummaryCurrentTermStatistics?
)
