package com.gdavidpb.tuindice.subjects.presentation.model

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab

data class SubjectDetailItem(
	val id: String,
	val name: String,
	val creditsText: String,
	val gradingModeText: String?,
	val generatedAtText: String,
	val selectedTab: SubjectSegmentTab,
	val hasSegmentTabs: Boolean,
	val chartMode: ChartMode,
	val careerSegment: SegmentItem?,
	val globalSegment: SegmentItem?
) {
	val selectedSegment: SegmentItem?
		get() = when (selectedTab) {
			SubjectSegmentTab.CAREER -> careerSegment ?: globalSegment
			SubjectSegmentTab.GLOBAL -> globalSegment ?: careerSegment
		}

	enum class ChartMode {
		NUMERIC_GRADES,
		QUALITATIVE_OUTCOMES
	}

	data class SegmentItem(
		val studentsText: String,
		val attemptsText: String,
		val difficultyScoreText: String,
		val difficultyBandText: String,
		val firstAttemptPassRateText: String,
		val approvalRateText: String,
		val failureRateText: String,
		val withdrawalRateText: String,
		val latestApprovedCount: Int,
		val latestFailedCount: Int,
		val latestRetiredCount: Int,
		val latestUnreportedCount: Int,
		val medianGrade: Double?,
		val stddevGrade: Double?,
		val latestGradeBins: List<GradeBinItem>,
		val attemptsToPassBins: List<AttemptBinItem>
	)

	data class GradeBinItem(
		val grade: Int,
		val count: Int
	)

	data class AttemptBinItem(
		val bucket: String,
		val count: Int
	)
}
