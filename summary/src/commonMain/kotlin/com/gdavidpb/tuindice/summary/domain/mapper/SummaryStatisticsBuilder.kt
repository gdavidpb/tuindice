package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordPaceEngine
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.summary.domain.model.SUBJECT_POINTS_TOTAL
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryCurrentTermStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummaryEvaluationTypeStatistic
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import kotlinx.datetime.LocalDate

// The closed terms as the record states them, with the pace they set, the best and the worst of them and the pensum
// progress. The term in progress comes as it is read, and it is absent when none is in progress.
internal fun summaryStatisticsOf(
	closed: RecordStatistics,
	pensumProgress: SummaryCreditsProgress?,
	currentTerm: SummaryCurrentTermStatistics?
): SummaryStatistics {
	val terms = closed.terms

	return SummaryStatistics(
		closed = closed,
		// The regular terms only, in order: the July - August term is shown but does not count toward the pace. A
		// regular term with no average still counts the credits it approved, since those credits are real whatever
		// index the term shows.
		pace = RecordPaceEngine.paceOf(
			approvedCreditsByRegularTerm = terms.filter(TermStatistic::isRegular).map(TermStatistic::approvedCredits),
			pensumApprovedCredits = pensumProgress?.approvedCredits,
			pensumTotalCredits = pensumProgress?.totalCredits
		),
		bestTerm = bestTermOf(terms),
		worstTerm = worstTermOf(terms),
		lastClosedTerm = terms.lastOrNull(),
		pensumProgress = pensumProgress,
		currentTerm = currentTerm
	)
}

// The highest index among the regular terms that have one; on a tie, the most recent term. A term with no index counts
// neither as the best nor as the worst, and neither does the July - August term: one or two intensive subjects are no
// fair match for a full term.
private fun bestTermOf(terms: List<TermStatistic>): TermStatistic? {
	return terms
		.filter(::isRankable)
		.maxWithOrNull(compareBy<TermStatistic>({ term -> term.index }, { term -> term.termOrder }))
}

// The lowest index among the regular terms that have one; on a tie, the most recent term.
private fun worstTermOf(terms: List<TermStatistic>): TermStatistic? {
	return terms
		.filter(::isRankable)
		.minWithOrNull(compareBy<TermStatistic>({ term -> term.index }).thenByDescending { term -> term.termOrder })
}

private fun isRankable(term: TermStatistic): Boolean = term.isRegular && term.index != null

// What the term in progress says of its evaluations, for the day it is read on. These are the evaluations of the term's
// subjects, the same the hub reads. Until they were downloaded nothing is known of them: the lists are left empty and
// the count is zero, and the fraction is null because the subjects of the term say nothing either.
internal fun currentTermStatisticsOf(
	term: SummaryTerm.InProgress,
	evaluations: List<Evaluation>,
	today: LocalDate
): SummaryCurrentTermStatistics {
	val known = if (term.hasSyncedEvaluations) evaluations else emptyList()

	return SummaryCurrentTermStatistics(
		periodCode = term.periodCode,
		periodYear = term.periodYear,
		evaluationTypes = evaluationTypesOf(known),
		evaluatedFraction = evaluatedFractionOf(term),
		overdueWithoutGrade = known.count { evaluation -> evaluation.stateOn(today) == EvaluationState.OVERDUE },
		hasSyncedEvaluations = term.hasSyncedEvaluations
	)
}

// Of the points the subjects of the term are out of, how many its evaluations already cover. Each subject counts for
// no more than its own total, the way its row shows it. Nothing when the term has no subject, or when the evaluations
// are not known.
private fun evaluatedFractionOf(term: SummaryTerm.InProgress): Double? {
	if (!term.hasSyncedEvaluations || term.subjects.isEmpty()) return null

	val evaluated = term.subjects.sumOf(SummarySubjectPoints::evaluatedOfTotal)

	return evaluated / (SUBJECT_POINTS_TOTAL * term.subjects.size)
}

// One entry per type the evaluations have, graded or not, in the order the types are declared.
private fun evaluationTypesOf(evaluations: List<Evaluation>): List<SummaryEvaluationTypeStatistic> {
	val byType = evaluations.groupBy(Evaluation::type)

	return EvaluationType.entries.mapNotNull { type ->
		byType[type]?.let { typeEvaluations -> typeStatisticOf(type = type, evaluations = typeEvaluations) }
	}
}

// The weighted performance of a type: the grades of its graded evaluations over the maximum grades of those same ones.
private fun typeStatisticOf(type: EvaluationType, evaluations: List<Evaluation>): SummaryEvaluationTypeStatistic {
	val graded = evaluations.mapNotNull { evaluation -> evaluation.grade?.let { grade -> grade to evaluation.maxGrade } }

	return SummaryEvaluationTypeStatistic(
		type = type,
		earned = graded.sumOf { (grade, _) -> grade },
		evaluated = graded.sumOf { (_, maxGrade) -> maxGrade },
		gradedCount = graded.size,
		totalCount = evaluations.size
	)
}
