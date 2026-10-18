package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordPaceEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReport
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportSubject

/**
 * The report of the term an insight is about, read from the record's projection with the academic engines: every figure
 * is the engines', never the model's. Null when the record holds no term with that key, so the screen says it has no
 * report for it. [closed] is the statistics of the record's closed terms, read from the same projection.
 */
internal fun termReportOf(
	insight: AssistantTermInsight,
	projection: RecordProjection,
	closed: RecordStatistics
): SummaryTermReport? {
	val terms = projection.terms.sortedWith(compareBy(TermProjection::termOrder, TermProjection::id))
	val position = terms.indexOfFirst { projected -> projected.termKey == insight.termKey }
	if (position < 0) return null

	val reported = terms[position]
	val term = RecordStatisticsEngine.termStatisticOf(reported)
	val previous = terms.getOrNull(position - 1)?.let { previousTerm ->
		RecordStatisticsEngine.termStatisticOf(previousTerm)
	}

	// The pace counts the regular terms the chart draws: the closed ones, and the term itself when it is regular and not
	// closed yet. The July - August term is never among them.
	val paceTerms = (
			closed.terms.filter { closedTerm -> closedTerm.isRegular && closedTerm.termKey != term.termKey } +
					listOfNotNull(term.takeIf(TermStatistic::isRegular))
			).sortedBy(TermStatistic::termOrder)

	return SummaryTermReport(
		insightId = insight.id,
		termId = insight.entityRefs.firstOrNull { ref -> ref.kind == AssistantEntityKind.TERM }?.id,
		termKey = insight.termKey,
		title = insight.title,
		sections = insight.sections,
		nextActions = insight.nextActions,
		term = term,
		previousTerm = previous,
		indexDelta = differenceOf(term.index, previous?.index),
		cumulativeDelta = differenceOf(term.cumulativeIndex, previous?.cumulativeIndex),
		subjects = reported.attempts
			.sortedWith(compareBy(AttemptProjection::subjectCode, AttemptProjection::id))
			.map { attempt -> attempt.toSummaryTermReportSubject() },
		creditsOfTerm = reported.attempts.filterNot(::isRetired).sumOf { attempt -> attempt.credits },
		paceTerms = paceTerms,
		averageCredits = RecordPaceEngine.paceOf(
			approvedCreditsByRegularTerm = paceTerms.map(TermStatistic::approvedCredits),
			pensumApprovedCredits = null,
			pensumTotalCredits = null
		).averageCredits
	)
}

/**
 * The line the summary offers for the newest report the student has not opened yet, or null when every report was
 * opened. [insights] come from the service; the newest by its creation instant is the one the line is about.
 */
internal fun termReportEntryOf(
	insights: List<AssistantTermInsight>,
	seenIds: Set<String>
): SummaryTermReportEntry? {
	val insight = insights
		.filter { candidate -> candidate.id !in seenIds }
		.maxByOrNull { candidate -> candidate.createdAtMillis }
		?: return null

	return SummaryTermReportEntry(
		insightId = insight.id,
		termKey = insight.termKey,
		periodLabel = termLabelOf(termKey = insight.termKey),
		subtitle = insight.title
	)
}

/**
 * The name of a term as the screens write it, "Abr - Jul 2026", read from its key ("2026-APR_JUL"). A key that names no
 * period the app knows is written as it is.
 */
internal fun termLabelOf(termKey: String): String {
	val year = termKey.substringBefore(KEY_SEPARATOR)
	val period = AcademicTermPeriod.entries.firstOrNull { candidate ->
		candidate.name == termKey.substringAfter(KEY_SEPARATOR, missingDelimiterValue = "")
	}

	return period?.let { "${it.shortLabel} $year" } ?: termKey
}

private fun AttemptProjection.toSummaryTermReportSubject(): SummaryTermReportSubject {
	return SummaryTermReportSubject(
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		outcome = outcomeOf(),
		numericGrade = score.numericValue,
		symbolicGrade = score.symbolicValue
	)
}

// What the term says of a subject: a withdrawn one is withdrawn whatever its result, and a result that is still pending
// (or unreported) is pending.
private fun AttemptProjection.outcomeOf(): SummaryTermReportOutcome {
	return when {
		isRetired(this) -> SummaryTermReportOutcome.WITHDRAWN
		outcome == AttemptOutcome.APPROVED -> SummaryTermReportOutcome.APPROVED
		outcome == AttemptOutcome.FAILED -> SummaryTermReportOutcome.FAILED
		else -> SummaryTermReportOutcome.PENDING
	}
}

// The same rule the statistics engine applies to a retired attempt.
private fun isRetired(attempt: AttemptProjection): Boolean {
	return attempt.withdrawn || attempt.outcome == AttemptOutcome.RETIRED
}

private fun differenceOf(current: Double?, previous: Double?): Double? {
	return if (current != null && previous != null) current - previous else null
}

private const val KEY_SEPARATOR = "-"
