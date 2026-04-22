package com.gdavidpb.tuindice.subjects.data.model

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectStatsSegmentResponse(
	@SerialName("sample_students") val sampleStudents: Int,
	@SerialName("closed_attempts") val closedAttempts: Int,
	@SerialName("numeric_latest_students") val numericLatestStudents: Int,
	@SerialName("latest_approved_count") val latestApprovedCount: Int,
	@SerialName("latest_failed_count") val latestFailedCount: Int,
	@SerialName("latest_retired_count") val latestRetiredCount: Int,
	@SerialName("latest_unreported_count") val latestUnreportedCount: Int,
	@SerialName("average_grade") val averageGrade: Double? = null,
	@SerialName("median_grade") val medianGrade: Double? = null,
	@SerialName("stddev_grade") val stddevGrade: Double? = null,
	@SerialName("first_attempt_pass_rate") val firstAttemptPassRate: Double? = null,
	@SerialName("approval_rate") val approvalRate: Double? = null,
	@SerialName("latest_failure_rate") val latestFailureRate: Double? = null,
	@SerialName("latest_withdrawal_rate") val latestWithdrawalRate: Double? = null,
	@SerialName("retake_rate") val retakeRate: Double? = null,
	@SerialName("avg_attempts_to_pass") val avgAttemptsToPass: Double? = null,
	@SerialName("median_attempts_to_pass") val medianAttemptsToPass: Double? = null,
	@SerialName("difficulty_score") val difficultyScore: Int? = null,
	@SerialName("difficulty_band") val difficultyBand: SubjectDifficultyBand? = null,
	@SerialName("first_closed_term_start_at") val firstClosedTermStartAt: Long? = null,
	@SerialName("last_closed_term_start_at") val lastClosedTermStartAt: Long? = null,
	@SerialName("latest_grade_bins") val latestGradeBins: List<SubjectGradeBinResponse> = emptyList(),
	@SerialName("all_grade_bins") val allGradeBins: List<SubjectGradeBinResponse> = emptyList(),
	@SerialName("attempts_to_pass_bins") val attemptsToPassBins: List<SubjectAttemptBinResponse> = emptyList()
)
