package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * What the screen shows under the profile. Every part is absent (empty or null) until there is
 * something to say: the career is null until the hub was first observed, so the screen does not
 * announce "no pensum" for the instant before it knows.
 */
@Immutable
data class SummaryHubItem(
	val attention: List<SummaryAttentionItem> = emptyList(),
	val week: SummaryWeekItem? = null,
	val term: SummaryTermItem? = null,
	val career: SummaryCareerItem? = null
)
