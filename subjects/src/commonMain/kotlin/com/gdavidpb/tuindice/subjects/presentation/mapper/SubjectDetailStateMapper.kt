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
import org.jetbrains.compose.resources.getString
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_credits_pattern
import tuindice.subjects.generated.resources.subjects_difficulty_high
import tuindice.subjects.generated.resources.subjects_difficulty_low
import tuindice.subjects.generated.resources.subjects_difficulty_medium
import tuindice.subjects.generated.resources.subjects_difficulty_very_high
import tuindice.subjects.generated.resources.subjects_generated_at
import tuindice.subjects.generated.resources.subjects_grading_qualitative
import tuindice.subjects.generated.resources.subjects_metric_empty
import tuindice.subjects.generated.resources.subjects_percent_pattern
import tuindice.subjects.generated.resources.subjects_score_pattern
import kotlin.math.roundToInt
import kotlin.time.Instant

suspend fun SubjectDetailResult.toViewState(): SubjectDetail.State {
	val textMapping = getSubjectDetailTextMapping()
	return when (this) {
		is SubjectDetailResult.Ready -> {
			val selectedTab = if (detail.careerSegment != null) {
				SubjectSegmentTab.CAREER
			} else {
				SubjectSegmentTab.GLOBAL
			}
			SubjectDetail.State.Content(
				detail = detail.toItem(
					selectedTab = selectedTab,
					textMapping = textMapping
				)
			)
		}

		is SubjectDetailResult.Unavailable ->
			SubjectDetail.State.Unavailable(subjectCode = subjectCode)
	}
}

internal fun SubjectDetailItem.withSelectedTab(tab: SubjectSegmentTab): SubjectDetailItem {
	return copy(selectedTab = tab)
}

private suspend fun getSubjectDetailTextMapping(): SubjectDetailTextMapping {
	return SubjectDetailTextMapping(
		creditsPattern = getString(Res.string.subjects_credits_pattern),
		qualitativeGradingLabel = getString(Res.string.subjects_grading_qualitative),
		generatedAtPattern = getString(Res.string.subjects_generated_at),
		emptyMetricText = getString(Res.string.subjects_metric_empty),
		percentPattern = getString(Res.string.subjects_percent_pattern),
		scorePattern = getString(Res.string.subjects_score_pattern),
		lowDifficultyLabel = getString(Res.string.subjects_difficulty_low),
		mediumDifficultyLabel = getString(Res.string.subjects_difficulty_medium),
		highDifficultyLabel = getString(Res.string.subjects_difficulty_high),
		veryHighDifficultyLabel = getString(Res.string.subjects_difficulty_very_high)
	)
}

private data class SubjectDetailTextMapping(
	val creditsPattern: String,
	val qualitativeGradingLabel: String,
	val generatedAtPattern: String,
	val emptyMetricText: String,
	val percentPattern: String,
	val scorePattern: String,
	val lowDifficultyLabel: String,
	val mediumDifficultyLabel: String,
	val highDifficultyLabel: String,
	val veryHighDifficultyLabel: String
)

private fun SubjectDetailModel.toItem(
	selectedTab: SubjectSegmentTab,
	textMapping: SubjectDetailTextMapping
): SubjectDetailItem {
	return SubjectDetailItem(
		id = id,
		name = name,
		creditsText = textMapping.creditsPattern.formatIntArg(credits),
		gradingModeText = if (gradingMode == GradingMode.QUALITATIVE_PASS_FAIL)
			textMapping.qualitativeGradingLabel
		else
			null,
		generatedAtText = textMapping.generatedAtPattern.formatStringArg(generatedAt.toDateText()),
		selectedTab = selectedTab,
		hasSegmentTabs = careerSegment != null && globalSegment != null,
		chartMode = when (gradingMode) {
			GradingMode.QUALITATIVE_PASS_FAIL -> SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES
			else -> SubjectDetailItem.ChartMode.NUMERIC_GRADES
		},
		careerSegment = careerSegment?.toItem(textMapping = textMapping),
		globalSegment = globalSegment?.toItem(textMapping = textMapping)
	)
}

private fun SubjectStatsSegment.toItem(textMapping: SubjectDetailTextMapping): SubjectDetailItem.SegmentItem {
	return SubjectDetailItem.SegmentItem(
		studentsText = sampleStudents.toCompactCountText(),
		attemptsText = closedAttempts.toCompactCountText(),
		difficultyScoreText = difficultyScore.toScoreText(textMapping = textMapping),
		difficultyBandText = difficultyBand.toDisplayText(textMapping = textMapping),
		firstAttemptPassRateText = firstAttemptPassRate.toPercentText(textMapping = textMapping),
		approvalRateText = approvalRate.toPercentText(textMapping = textMapping),
		failureRateText = latestFailureRate.toPercentText(textMapping = textMapping),
		withdrawalRateText = latestWithdrawalRate.toPercentText(textMapping = textMapping),
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

private fun Double?.toPercentText(textMapping: SubjectDetailTextMapping): String {
	return if (this == null) {
		textMapping.emptyMetricText
	} else {
		textMapping.percentPattern.formatIntArg((this * 100).roundToInt())
	}
}

private fun Int?.toScoreText(textMapping: SubjectDetailTextMapping): String {
	return if (this == null) {
		textMapping.emptyMetricText
	} else {
		textMapping.scorePattern.formatIntArg(this)
	}
}

private fun SubjectDifficultyBand?.toDisplayText(textMapping: SubjectDetailTextMapping): String {
	return when (this) {
		SubjectDifficultyBand.LOW -> textMapping.lowDifficultyLabel
		SubjectDifficultyBand.MEDIUM -> textMapping.mediumDifficultyLabel
		SubjectDifficultyBand.HIGH -> textMapping.highDifficultyLabel
		SubjectDifficultyBand.VERY_HIGH -> textMapping.veryHighDifficultyLabel
		null -> textMapping.emptyMetricText
	}
}

private fun String.formatIntArg(value: Int): String {
	return replace("%1${'$'}d", value.toString())
		.replace("%%", "%")
}

private fun String.formatStringArg(value: String): String {
	return replace("%1${'$'}s", value)
}
