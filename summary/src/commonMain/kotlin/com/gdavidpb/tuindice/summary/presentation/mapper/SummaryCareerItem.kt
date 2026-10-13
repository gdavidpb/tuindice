package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_credits_rest
import tuindice.summary.generated.resources.summary_career_description
import tuindice.summary.generated.resources.summary_career_percent
import kotlin.math.roundToInt

private const val PERCENT = 100

/**
 * The progress of the pensum, or the invitation to open it when none is cached (or it has no
 * credits to measure against).
 */
fun PensumCreditsProgress?.toSummaryCareerItem(): SummaryCareerItem {
	if (this == null || totalCredits <= 0) return SummaryCareerItem.Invite

	val approved = approvedCredits.coerceIn(0, totalCredits)
	val percent = (approved * PERCENT.toDouble() / totalCredits).roundToInt()

	return SummaryCareerItem.Progress(
		valueText = approved.toString(),
		restText = UiText.Resource(Res.string.summary_career_credits_rest, listOf(totalCredits)),
		percentText = UiText.Resource(Res.string.summary_career_percent, listOf(percent)),
		fraction = approved.toFloat() / totalCredits,
		description = UiText.Resource(Res.string.summary_career_description, listOf(approved, totalCredits, percent))
	)
}
