package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * The pace of the regular terms. [averageCredits] is the approved credits per regular term, or null without any regular
 * term. [remainingTerms] is how many more regular terms the pensum takes at that pace, or null when it cannot be
 * projected yet.
 */
data class PaceStatistics(
	val averageCredits: Double?,
	val remainingTerms: Int?
)
