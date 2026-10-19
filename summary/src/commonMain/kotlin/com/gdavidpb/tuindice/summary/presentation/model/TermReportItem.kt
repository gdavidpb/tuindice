package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

/**
 * What the term report screen shows of one report, in the order of the screen: the figures of the term, its subjects
 * and its pace, then what the assistant wrote and the actions that follow it. [title] is the report's own title, shown
 * when it has no sections. [isShareable] says whether its term can be shared as an image, and [confirmedFeedback] is
 * the thumb the student's last vote was confirmed with.
 */
@Immutable
data class TermReportItem(
	val insightId: String,
	val termId: String?,
	val termKey: String,
	val periodLabel: String,
	val title: String,
	val hero: TermReportHeroItem,
	val subjects: TermReportSubjectsItem,
	val pace: TermReportPaceItem,
	val sections: List<TermReportSectionItem>,
	val nextActions: List<TermReportActionItem>,
	val isShareable: Boolean,
	val confirmedFeedback: Boolean?,
	val assistantContext: AssistantScreenContext
)
