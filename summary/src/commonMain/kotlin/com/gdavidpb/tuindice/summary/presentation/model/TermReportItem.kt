package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome

/**
 * What the term report screen shows of one report, in the order of the screen: the figures of the term, its subjects
 * and its pace, then what the assistant wrote and the actions that follow it. [title] is the report's own title, shown
 * when it has no sections.
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
	val nextActions: List<TermReportActionItem>
)

/** The index of the term and its cumulative index, each with the change from the term before it when there is one. */
@Immutable
data class TermReportHeroItem(
	val title: UiText,
	val index: String?,
	val indexDelta: TermReportDeltaItem?,
	val cumulativeIndex: String?,
	val cumulativeDelta: TermReportDeltaItem?
)

/** A change as the chip shows it: its words and the tone that colours them. */
@Immutable
data class TermReportDeltaItem(
	val text: UiText,
	val tone: TermReportDeltaTone
)

enum class TermReportDeltaTone {
	POSITIVE,
	NEGATIVE,
	NEUTRAL
}

/** The subjects of the term: the credits it approved, the count of each outcome, and one row per subject. */
@Immutable
data class TermReportSubjectsItem(
	val credits: UiText,
	val legend: List<TermReportLegendItem>,
	val rows: List<TermReportSubjectItem>
)

@Immutable
data class TermReportLegendItem(
	val outcome: SummaryTermReportOutcome,
	val text: UiText
)

/**
 * One subject of the term: its code and name, its grade as the badge writes it (null when it has none) and its outcome.
 */
@Immutable
data class TermReportSubjectItem(
	val subjectCode: String,
	val subjectName: String,
	val grade: String?,
	val outcome: SummaryTermReportOutcome
)

/** The pace of the regular terms: the bars under their average, or the words that say why there is no pace yet. */
@Immutable
sealed interface TermReportPaceItem {
	data class Empty(
		val text: UiText
	) : TermReportPaceItem

	data class Data(
		val bars: List<SummaryStatisticsBarItem>,
		val reference: SummaryStatisticsReferenceItem,
		val currentLegend: UiText,
		val averageLegend: UiText
	) : TermReportPaceItem
}

/** One section of what the assistant wrote: its title and its text, both as the model wrote them. */
@Immutable
data class TermReportSectionItem(
	val title: String,
	val text: String
)

/** A button of "Qué sigue": its [label], the screen it opens ([target]) and the records that screen needs. */
@Immutable
data class TermReportActionItem(
	val label: String,
	val target: AssistantNavTarget,
	val entityRefs: List<AssistantEntityRef>
)
