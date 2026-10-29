package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem

// What the assistant reads of the statistics screen: the subjects by outcome, the credits the pensum has approved and
// the pace of the regular terms, as the figures the screen draws. The screen has no filter, so none is named.
internal fun summaryStatisticsAssistantContext(content: SummaryStatistics.State.Content): AssistantScreenContext {
	val statistics = content.statistics
	val subjects = statistics.subjects.content as? SummaryStatisticsSubjectsItem.Content.Data
	val credits = statistics.credits.content as? SummaryStatisticsCreditsItem.Content.Data
	val pace = statistics.pace.content as? SummaryStatisticsPaceItem.Content.Data

	return AssistantScreenContext(
		screen = AssistantScreen.STATISTICS,
		visibleFields = buildMap {
			subjects?.segments?.forEach { segment ->
				put(segment.outcome.toAssistantField(), segment.value.toInt().toString())
			}

			if (credits != null) put("approvedCredits", credits.figure.value)

			if (pace != null) put("averageApprovedCreditsPerTerm", pace.figure.value)
		},
		filters = emptyMap()
	)
}

// The name each outcome has in what the assistant reads: how many subjects ended that way.
private fun SummaryStatisticsOutcome.toAssistantField(): String {
	return when (this) {
		SummaryStatisticsOutcome.APPROVED -> "approvedSubjects"
		SummaryStatisticsOutcome.FAILED -> "failedSubjects"
		SummaryStatisticsOutcome.WITHDRAWN -> "withdrawnSubjects"
	}
}
