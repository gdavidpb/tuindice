package com.gdavidpb.tuindice.subjects.domain.model

data class SubjectStatsSegment(
	val sampleStudents: Int,
	val closedAttempts: Int,
	val numericLatestStudents: Int,
	val latestApprovedCount: Int,
	val latestFailedCount: Int,
	val latestRetiredCount: Int,
	val latestUnreportedCount: Int,
	val averageGrade: Double? = null,
	val medianGrade: Double? = null,
	val stddevGrade: Double? = null,
	val firstAttemptPassRate: Double? = null,
	val approvalRate: Double? = null,
	val latestFailureRate: Double? = null,
	val latestWithdrawalRate: Double? = null,
	val retakeRate: Double? = null,
	val avgAttemptsToPass: Double? = null,
	val medianAttemptsToPass: Double? = null,
	val difficultyScore: Int? = null,
	val difficultyBand: SubjectDifficultyBand? = null,
	val firstClosedTermStartAt: Long? = null,
	val lastClosedTermStartAt: Long? = null,
	val latestGradeBins: List<SubjectGradeBin> = emptyList(),
	val allGradeBins: List<SubjectGradeBin> = emptyList(),
	val attemptsToPassBins: List<SubjectAttemptBin> = emptyList()
)
