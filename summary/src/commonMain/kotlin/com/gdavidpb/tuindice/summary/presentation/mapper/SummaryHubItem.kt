package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem

/**
 * What the screen shows of the hub. The career is always present once the hub was read: either the
 * progress or the invitation to open the pensum. The week and the term are present whenever the record
 * is ready, each saying what it has or inviting to what it lacks; without a ready record they are left out.
 */
fun SummaryHub.toSummaryHubItem(): SummaryHubItem {
	return SummaryHubItem(
		attention = attention.map { point -> point.toSummaryAttentionItem() },
		week = week?.toSummaryWeekItem(),
		term = term?.toSummaryTermItem(),
		career = career.toSummaryCareerItem()
	)
}
