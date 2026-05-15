package com.gdavidpb.tuindice.subjects.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

internal fun SubjectDetailResult.toViewState(): SubjectDetail.State {
	return when (this) {
		is SubjectDetailResult.Ready -> {
			val selectedTab = if (detail.careerSegment != null) {
				SubjectSegmentTab.CAREER
			} else {
				SubjectSegmentTab.GLOBAL
			}
			SubjectDetail.State.Content(
				detail = detail.toItem(selectedTab = selectedTab)
			)
		}

		is SubjectDetailResult.Unavailable ->
			SubjectDetail.State.Unavailable(subjectCode = subjectCode)
	}
}

internal fun SubjectDetailItem.withSelectedTab(tab: SubjectSegmentTab): SubjectDetailItem {
	return copy(selectedTab = tab)
}

private fun SubjectDetailModel.toItem(selectedTab: SubjectSegmentTab): SubjectDetailItem {
	return SubjectDetailItem(
		id = id,
		name = name,
		creditsText = "$credits UC",
		gradingModeText = if (gradingMode == GradingMode.QUALITATIVE_PASS_FAIL) "Cualitativa" else null,
		generatedAtText = "Actualizado ${generatedAt.toDateText()}",
		selectedTab = selectedTab,
		hasSegmentTabs = careerSegment != null && globalSegment != null,
		chartMode = when (gradingMode) {
			GradingMode.QUALITATIVE_PASS_FAIL -> SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES
			else -> SubjectDetailItem.ChartMode.NUMERIC_GRADES
		},
		careerSegment = careerSegment?.toItem(),
		globalSegment = globalSegment?.toItem()
	)
}

private fun SubjectStatsSegment.toItem(): SubjectDetailItem.SegmentItem {
	return SubjectDetailItem.SegmentItem(
		studentsText = sampleStudents.toCompactCountText(),
		attemptsText = closedAttempts.toCompactCountText(),
		difficultyScoreText = difficultyScore.toScoreText(),
		difficultyBandText = difficultyBand.toDisplayText(),
		firstAttemptPassRateText = firstAttemptPassRate.toPercentText(),
		approvalRateText = approvalRate.toPercentText(),
		failureRateText = latestFailureRate.toPercentText(),
		withdrawalRateText = latestWithdrawalRate.toPercentText(),
		latestApprovedCount = latestApprovedCount,
		latestFailedCount = latestFailedCount,
		latestRetiredCount = latestRetiredCount,
		latestUnreportedCount = latestUnreportedCount,
		medianGrade = medianGrade,
		stddevGrade = stddevGrade,
		latestGradeBins = latestGradeBins.map(SubjectGradeBin::toItem),
		attemptsToPassBins = attemptsToPassBins.map(SubjectAttemptBin::toItem)
	)
}

private fun SubjectGradeBin.toItem(): SubjectDetailItem.GradeBinItem {
	return SubjectDetailItem.GradeBinItem(
		grade = grade,
		count = count
	)
}

private fun SubjectAttemptBin.toItem(): SubjectDetailItem.AttemptBinItem {
	return SubjectDetailItem.AttemptBinItem(
		bucket = bucket,
		count = count
	)
}

private fun Long.toDateText(): String {
	val localDate = Instant.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
	return "${localDate.day}/${localDate.month.number}/${localDate.year}"
}

private fun Double?.toPercentText(): String {
	return if (this == null) "--" else "${(this * 100).roundToInt()}%"
}

private fun Int?.toScoreText(): String {
	return if (this == null) "--" else "$this/100"
}

private fun SubjectDifficultyBand?.toDisplayText(): String {
	return when (this) {
		SubjectDifficultyBand.LOW -> "Baja"
		SubjectDifficultyBand.MEDIUM -> "Media"
		SubjectDifficultyBand.HIGH -> "Alta"
		SubjectDifficultyBand.VERY_HIGH -> "Muy alta"
		null -> "--"
	}
}
