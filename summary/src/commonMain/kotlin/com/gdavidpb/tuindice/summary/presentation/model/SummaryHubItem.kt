package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * What the screen shows under the profile. The week and the term are null until a ready record says
 * what they are, and they stay null without one; once they are there, each carries what it has or
 * the card that invites to what it lacks. The career is null until the hub was first observed, so the
 * screen does not announce "no pensum" for the instant before it knows.
 */
@Immutable
data class SummaryHubItem(
	val attention: List<SummaryAttentionItem> = emptyList(),
	val week: SummaryWeekItem? = null,
	val term: SummaryTermItem? = null,
	val career: SummaryCareerItem? = null
)
