package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem

private const val ASSISTANT_GRADE_DECIMALS = 4

// What the assistant reads of the summary screen: the term in progress, the figures the hub draws of it, and the
// career, the attention and the statistics the hub shows under the profile. It reads what the screen draws, so
// each figure is described as the student sees it. The hub draws no index for the term in progress, and the total
// of the credits only inside a text, so termIndex and totalCredits are not named.
internal fun summaryAssistantContext(content: Summary.State.Content): AssistantScreenContext {
	val hub = content.hub
	val term = hub.term
	val termId = term?.termId
	val week = term?.week
	val career = hub.career as? SummaryCareerItem.Progress
	val cumulativeIndex = (hub.statistics as? SummaryStatisticsSectionItem.Figures)?.grade

	return AssistantScreenContext(
		screen = AssistantScreen.SUMMARY,
		entities = buildList {
			if (termId != null) add(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = termId))
		},
		visibleFields = buildMap {
			// The link of the term in progress is its name, a text the screen already holds. The other links say
			// "Registro" and name no term, so they name none here.
			val currentTermLabel = (term?.linkText as? UiText.Raw)?.value
			if (currentTermLabel != null) put("currentTermLabel", currentTermLabel)

			put("attentionCount", hub.attention.size.toString())

			if (career != null) put("approvedCredits", career.valueText)

			if (cumulativeIndex != null) {
				put("cumulativeIndex", cumulativeIndex.formatGrade(decimals = ASSISTANT_GRADE_DECIMALS))
			}

			if (week != null) {
				put("week", week.week.toString())
				put("totalWeeks", week.totalWeeks.toString())
			}
		},
		filters = emptyMap()
	)
}
