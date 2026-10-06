package com.gdavidpb.tuindice.subjects.testing

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem

/**
 * Presentation fixtures for the subject detail UI tests. Each one returns a complete
 * item; tests derive their variant with `copy(...)` so only the fields under test show.
 */
fun subjectSegmentItem(): SubjectDetailItem.SegmentItem {
	return SubjectDetailItem.SegmentItem(
		studentsText = "18",
		attemptsText = "24",
		difficultyScoreText = "41 / 100",
		difficultyBandText = "Media",
		firstAttemptPassRateText = "55%",
		approvalRateText = "72%",
		failureRateText = "22%",
		withdrawalRateText = "6%",
		latestApprovedCount = 12,
		latestFailedCount = 4,
		latestRetiredCount = 1,
		latestUnreportedCount = 1,
		medianGrade = 4.0,
		stddevGrade = 0.8,
		latestGradeBins = listOf(
			SubjectDetailItem.GradeBinItem(grade = 1, count = 1),
			SubjectDetailItem.GradeBinItem(grade = 5, count = 3)
		),
		attemptsToPassBins = listOf(
			SubjectDetailItem.AttemptBinItem(bucket = "1", count = 8),
			SubjectDetailItem.AttemptBinItem(bucket = "3_plus", count = 2)
		)
	)
}

fun globalSubjectSegmentItem(): SubjectDetailItem.SegmentItem {
	return subjectSegmentItem().copy(
		studentsText = "2.4k",
		attemptsText = "3.7k",
		difficultyScoreText = "67 / 100",
		difficultyBandText = "Alta",
		firstAttemptPassRateText = "38%",
		approvalRateText = "51%",
		failureRateText = "34%",
		withdrawalRateText = "15%"
	)
}

fun emptySubjectSegmentItem(): SubjectDetailItem.SegmentItem {
	return SubjectDetailItem.SegmentItem(
		studentsText = "0",
		attemptsText = "0",
		difficultyScoreText = "--",
		difficultyBandText = "--",
		firstAttemptPassRateText = "--",
		approvalRateText = "--",
		failureRateText = "--",
		withdrawalRateText = "--",
		latestApprovedCount = 0,
		latestFailedCount = 0,
		latestRetiredCount = 0,
		latestUnreportedCount = 0,
		medianGrade = null,
		stddevGrade = null,
		latestGradeBins = emptyList(),
		attemptsToPassBins = emptyList()
	)
}

fun subjectDetailItem(): SubjectDetailItem {
	return SubjectDetailItem(
		id = "MAT101",
		name = "Calculo I",
		creditsText = "5 UC",
		gradingModeText = null,
		generatedAtText = "Actualizado 09/03/2024",
		selectedTab = SubjectSegmentTab.CAREER,
		hasSegmentTabs = false,
		chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES,
		careerSegment = subjectSegmentItem(),
		globalSegment = null
	)
}
