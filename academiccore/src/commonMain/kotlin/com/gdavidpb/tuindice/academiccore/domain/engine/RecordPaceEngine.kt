package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics

// One regular term shows an average, but a pace is not projected from a single term.
private const val MIN_REGULAR_TERMS_FOR_PACE = 2

/** The pace of a student over the regular terms, and how many of them the pensum still takes at that pace. */
object RecordPaceEngine {
	/**
	 * [approvedCreditsByRegularTerm] holds one entry per regular closed term with the credits it approved, zero when it
	 * approved none. The caller leaves out the July - August term. The pensum credits are plain numbers, null when the
	 * pensum is not known. The remaining terms are `ceil((total - approved) / average)`, worked out in integers so that
	 * a floating point error can never add a term.
	 */
	fun paceOf(
		approvedCreditsByRegularTerm: List<Int>,
		pensumApprovedCredits: Int?,
		pensumTotalCredits: Int?
	): PaceStatistics {
		val termCount = approvedCreditsByRegularTerm.size
		val approvedCredits = approvedCreditsByRegularTerm.sum()
		val averageCredits = if (termCount == 0) null else approvedCredits.toDouble() / termCount

		return PaceStatistics(
			averageCredits = averageCredits,
			remainingTerms = remainingTermsOf(
				pensumRemainingCredits = remainingCreditsOf(pensumApprovedCredits, pensumTotalCredits),
				termCount = termCount,
				approvedCredits = approvedCredits
			)
		)
	}

	private fun remainingTermsOf(pensumRemainingCredits: Int?, termCount: Int, approvedCredits: Int): Int? {
		val canProject = termCount >= MIN_REGULAR_TERMS_FOR_PACE && approvedCredits > 0

		return pensumRemainingCredits
			?.takeIf { remaining -> canProject && remaining > 0 }
			// remaining / (approved / termCount) is remaining * termCount / approved, so the division stays exact.
			?.let { remaining -> ceilDivision(remaining.toLong() * termCount, approvedCredits.toLong()).toInt() }
	}

	private fun remainingCreditsOf(pensumApprovedCredits: Int?, pensumTotalCredits: Int?): Int? {
		return pensumTotalCredits?.let { total ->
			pensumApprovedCredits?.let { approved -> total - approved }
		}
	}

	// Both operands are positive here, so rounding up is adding one less than the divisor before dividing.
	private fun ceilDivision(dividend: Long, divisor: Long): Long = (dividend + divisor - 1) / divisor
}
