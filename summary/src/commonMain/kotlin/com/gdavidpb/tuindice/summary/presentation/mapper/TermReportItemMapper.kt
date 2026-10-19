package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.foundation.layout.size
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReport
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportSubject
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaTone
import com.gdavidpb.tuindice.summary.presentation.model.TermReportHeroItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportLegendItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSectionItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectsItem
import org.jetbrains.compose.resources.StringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_term_report_delta_down
import tuindice.summary.generated.resources.a11y_term_report_delta_none
import tuindice.summary.generated.resources.a11y_term_report_delta_same
import tuindice.summary.generated.resources.a11y_term_report_delta_short_down
import tuindice.summary.generated.resources.a11y_term_report_delta_short_up
import tuindice.summary.generated.resources.a11y_term_report_delta_up
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_subjects_many
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_subjects_one
import tuindice.summary.generated.resources.summary_statistics_pace_tooltip_title
import tuindice.summary.generated.resources.term_report_delta_none
import tuindice.summary.generated.resources.term_report_delta_reference
import tuindice.summary.generated.resources.term_report_delta_short_down
import tuindice.summary.generated.resources.term_report_delta_short_up
import tuindice.summary.generated.resources.term_report_hero_title
import tuindice.summary.generated.resources.term_report_legend_approved_many
import tuindice.summary.generated.resources.term_report_legend_approved_one
import tuindice.summary.generated.resources.term_report_legend_failed_many
import tuindice.summary.generated.resources.term_report_legend_failed_one
import tuindice.summary.generated.resources.term_report_legend_withdrawn_many
import tuindice.summary.generated.resources.term_report_legend_withdrawn_one
import tuindice.summary.generated.resources.term_report_pace_average
import tuindice.summary.generated.resources.term_report_pace_current
import tuindice.summary.generated.resources.term_report_pace_empty
import tuindice.summary.generated.resources.term_report_subjects_credits
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TERM_REPORT_INDEX_DECIMALS = 4
private const val TERM_REPORT_DELTA_DECIMALS = 4
private const val TERM_REPORT_DELTA_SCALE = 10_000
private const val TERM_REPORT_AVERAGE_DECIMALS = 1

// The report as the screen draws it. Every figure is one the domain read from the record, written here as text only.
internal fun SummaryTermReport.toTermReportItem(): TermReportItem {
	val periodLabel = term.fullLabel()
	val hero = heroOf(report = this, periodLabel = periodLabel)
	val sectionItems = sections.map { section -> TermReportSectionItem(title = section.title, text = section.text) }

	return TermReportItem(
		insightId = insightId,
		termId = termId,
		termKey = termKey,
		periodLabel = periodLabel,
		title = title,
		hero = hero,
		subjects = subjectsOf(report = this),
		pace = paceOf(report = this),
		sections = sectionItems,
		nextActions = nextActions.map { action ->
			TermReportActionItem(
				label = action.label,
				target = action.target,
				icon = action.target.toActionIcon(),
				entityRefs = action.entityRefs
			)
		},
		isShareable = isShareable,
		confirmedFeedback = feedback,
		assistantContext = termReportAssistantContextOf(
			report = this,
			periodLabel = periodLabel,
			index = hero.index,
			sections = sectionItems
		)
	)
}

private fun heroOf(report: SummaryTermReport, periodLabel: String): TermReportHeroItem {
	val previous = report.previousTerm?.fullLabel()

	return TermReportHeroItem(
		title = UiText.Resource(Res.string.term_report_hero_title, listOf(termNameUnbreakable(periodLabel))),
		index = report.term.index?.formatGrade(decimals = TERM_REPORT_INDEX_DECIMALS),
		indexDelta = indexDeltaOf(delta = report.indexDelta, previousLabel = previous),
		cumulativeIndex = report.term.cumulativeIndex?.formatGrade(decimals = TERM_REPORT_INDEX_DECIMALS),
		cumulativeDelta = cumulativeDeltaOf(delta = report.cumulativeDelta)
	)
}

// The change of the index, measured from the term before: its size with the arrow, and that term's name written under
// the figure. A change that rounds to nothing at the printed decimals says there is none, the same as no change at all.
private fun indexDeltaOf(delta: Double?, previousLabel: String?): TermReportDeltaItem? {
	if (delta == null || previousLabel == null) return null

	val ticks = ticksOf(delta)
	val size = sizeOf(ticks)
	// The line under the figure keeps the term's name whole; the words for the screen reader read it as written.
	val reference = UiText.Resource(Res.string.term_report_delta_reference, listOf(termNameUnbreakable(previousLabel)))

	return when {
		ticks > 0 -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_short_up, listOf(size)),
			description = UiText.Resource(Res.string.a11y_term_report_delta_up, listOf(size, previousLabel)),
			tone = TermReportDeltaTone.POSITIVE,
			reference = reference
		)

		ticks < 0 -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_short_down, listOf(size)),
			description = UiText.Resource(Res.string.a11y_term_report_delta_down, listOf(size, previousLabel)),
			tone = TermReportDeltaTone.NEGATIVE,
			reference = reference
		)

		else -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_none),
			description = UiText.Resource(Res.string.a11y_term_report_delta_same, listOf(previousLabel)),
			tone = TermReportDeltaTone.NEUTRAL,
			reference = reference
		)
	}
}

// The change of the cumulative index: its size with the arrow, measured from the term before, as the chip beside it
// says. The chip says the change alone; the term is named by the index's chip.
private fun cumulativeDeltaOf(delta: Double?): TermReportDeltaItem? {
	if (delta == null) return null

	val ticks = ticksOf(delta)
	val size = sizeOf(ticks)

	return when {
		ticks > 0 -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_short_up, listOf(size)),
			description = UiText.Resource(Res.string.a11y_term_report_delta_short_up, listOf(size)),
			tone = TermReportDeltaTone.POSITIVE
		)

		ticks < 0 -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_short_down, listOf(size)),
			description = UiText.Resource(Res.string.a11y_term_report_delta_short_down, listOf(size)),
			tone = TermReportDeltaTone.NEGATIVE
		)

		else -> TermReportDeltaItem(
			text = UiText.Resource(Res.string.term_report_delta_none),
			description = UiText.Resource(Res.string.a11y_term_report_delta_none),
			tone = TermReportDeltaTone.NEUTRAL
		)
	}
}

// The change in ten-thousandths, rounded once: the sign of the arrow and the figure of the text both come from this
// integer, so a change of 4.1800 against 4.1799 is an up of 0.0001 and never an up written as 0.0000.
private fun ticksOf(delta: Double): Int {
	return (delta * TERM_REPORT_DELTA_SCALE).roundToInt()
}

// The size of a change as the chip writes it, from the same integer its arrow comes from. It is written from the
// integer itself: a Double this small would be written in scientific notation.
private fun sizeOf(ticks: Int): String {
	val size = abs(ticks)
	val whole = size / TERM_REPORT_DELTA_SCALE
	val fraction = (size % TERM_REPORT_DELTA_SCALE).toString().padStart(TERM_REPORT_DELTA_DECIMALS, '0')

	return "$whole.$fraction"
}

// The subjects of the term in order of code: the credits the term approved over the credits it holds, the count of each
// outcome for the legend, and one row per subject.
private fun subjectsOf(report: SummaryTermReport): TermReportSubjectsItem {
	val counts = report.subjects.groupingBy(SummaryTermReportSubject::outcome).eachCount()

	return TermReportSubjectsItem(
		credits = UiText.Resource(
			Res.string.term_report_subjects_credits,
			listOf(report.term.approvedCredits, report.creditsOfTerm)
		),
		legend = listOf(
			legendOf(
				outcome = SummaryTermReportOutcome.APPROVED,
				count = counts[SummaryTermReportOutcome.APPROVED] ?: 0,
				one = Res.string.term_report_legend_approved_one,
				many = Res.string.term_report_legend_approved_many
			),
			legendOf(
				outcome = SummaryTermReportOutcome.FAILED,
				count = counts[SummaryTermReportOutcome.FAILED] ?: 0,
				one = Res.string.term_report_legend_failed_one,
				many = Res.string.term_report_legend_failed_many
			),
			legendOf(
				outcome = SummaryTermReportOutcome.WITHDRAWN,
				count = counts[SummaryTermReportOutcome.WITHDRAWN] ?: 0,
				one = Res.string.term_report_legend_withdrawn_one,
				many = Res.string.term_report_legend_withdrawn_many
			)
		),
		rows = report.subjects.map { subject ->
			TermReportSubjectItem(
				subjectCode = subject.subjectCode,
				subjectName = subject.subjectName,
				grade = subject.numericGrade?.toString() ?: subject.symbolicGrade,
				tone = subject.outcome.toTone()
			)
		}
	)
}

private fun legendOf(
	outcome: SummaryTermReportOutcome,
	count: Int,
	one: StringResource,
	many: StringResource
): TermReportLegendItem {
	return TermReportLegendItem(
		tone = outcome.toTone(),
		text = countText(quantity = count, one = one, many = many, args = listOf(count))
	)
}

// The pace of the regular terms: one bar per regular term with the credits it approved, the term of the report
// highlighted, and the average as the line. Without a regular term there is no average, and the block says so.
private fun paceOf(report: SummaryTermReport): TermReportPaceItem {
	val average = report.averageCredits
		?: return TermReportPaceItem.Empty(text = UiText.Resource(Res.string.term_report_pace_empty))
	val axisLabels = axisLabelsOf(report.paceTerms)

	return TermReportPaceItem.Data(
		bars = report.paceTerms.map { term ->
			barOf(term = term, axisLabels = axisLabels, isHighlighted = term.termKey == report.termKey)
		},
		// The average is written by the legend under the chart: the line itself carries no label.
		reference = SummaryStatisticsReferenceItem(value = average.toFloat(), label = null),
		currentLegend = UiText.Resource(Res.string.term_report_pace_current, listOf(report.term.approvedCredits)),
		averageLegend = UiText.Resource(
			Res.string.term_report_pace_average,
			listOf(average.formatGrade(decimals = TERM_REPORT_AVERAGE_DECIMALS))
		)
	)
}

private fun barOf(
	term: TermStatistic,
	axisLabels: Map<String, String>,
	isHighlighted: Boolean
): SummaryStatisticsBarItem {
	return SummaryStatisticsBarItem(
		axisLabel = UiText.Raw(axisLabels.getValue(term.termKey)),
		value = term.approvedCredits.toFloat(),
		tooltip = SummaryStatisticsTooltipItem(
			title = UiText.Resource(
				Res.string.summary_statistics_pace_tooltip_title,
				listOf(term.fullLabel(), term.approvedCredits)
			),
			lines = listOf(
				countText(
					quantity = term.approvedSubjects,
					one = Res.string.summary_statistics_pace_tooltip_subjects_one,
					many = Res.string.summary_statistics_pace_tooltip_subjects_many,
					args = listOf(term.approvedSubjects)
				)
			)
		),
		isHighlighted = isHighlighted
	)
}
