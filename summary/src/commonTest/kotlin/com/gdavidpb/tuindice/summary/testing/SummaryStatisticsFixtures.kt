package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryCurrentTermStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummaryEvaluationTypeStatistic
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import kotlinx.datetime.LocalDate

// A term of the record as the statistics engine states it: [index] is its period average, and its cumulative index is
// the same. The July - August term is the one that is not regular. A test that needs another cumulative index copies
// the term.
fun statisticsTerm(
	year: Int,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.SEP_DEC,
	index: Double? = 4.0,
	approvedCredits: Int = 15
): TermStatistic {
	return TermStatistic(
		termKey = "closed-$year-${periodCode.name}",
		periodCode = periodCode,
		periodYear = year,
		termOrder = year * ORDER_YEAR_FACTOR + periodCode.sequence,
		kind = TermKind.HISTORICAL,
		index = index,
		cumulativeIndex = index,
		approvedCredits = approvedCredits,
		approvedSubjects = APPROVED_SUBJECTS,
		isRegular = periodCode != AcademicTermPeriod.JUL_AUG
	)
}

// The statistics of a record as the hub reads them, with no pace, no pensum and no term in progress: a test that needs
// one of them copies the statistics with the one it needs. The best and the worst regular terms are the ones with the
// highest and the lowest index, and the last closed term is the last of [terms].
fun statisticsOf(
	terms: List<TermStatistic> = emptyList(),
	subjects: SubjectOutcomes = SubjectOutcomes(
		approved = 0,
		failed = 0,
		withdrawn = 0,
		noResult = 0,
		approvedFirstTry = 0,
		firstTryBase = 0,
		repeated = 0
	),
	grades: GradeHistogram = GradeHistogram(countsByGrade = (1..MAX_GRADE).associateWith { 0 }, unreported = 0)
): SummaryStatistics {
	val ranked = terms.filter { term -> term.isRegular && term.index != null }

	return SummaryStatistics(
		closed = RecordStatistics(terms = terms, subjects = subjects, grades = grades),
		pace = PaceStatistics(averageCredits = null, remainingTerms = null),
		bestTerm = ranked.maxWithOrNull(
			compareBy<TermStatistic>({ term -> term.index ?: 0.0 }, { term -> term.termOrder })
		),
		worstTerm = ranked.minWithOrNull(
			compareBy<TermStatistic>({ term -> term.index ?: 0.0 }).thenByDescending { term -> term.termOrder }
		),
		lastClosedTerm = terms.lastOrNull(),
		pensumProgress = null,
		currentTerm = null
	)
}

// The evaluations of the term in progress as Summary reads them for the day. [fraction] is how much of the points of
// its subjects the evaluations cover, null while that is not known.
fun currentTermOf(
	types: List<SummaryEvaluationTypeStatistic> = emptyList(),
	fraction: Double? = null,
	overdue: Int = 0,
	hasSynced: Boolean = true
): SummaryCurrentTermStatistics {
	return SummaryCurrentTermStatistics(
		periodCode = AcademicTermPeriod.SEP_DEC,
		periodYear = CURRENT_YEAR,
		evaluationTypes = types,
		evaluatedFraction = fraction,
		overdueWithoutGrade = overdue,
		hasSyncedEvaluations = hasSynced
	)
}

fun evaluationTypeOf(
	type: EvaluationType,
	earned: Double,
	evaluated: Double,
	graded: Int,
	total: Int
): SummaryEvaluationTypeStatistic {
	return SummaryEvaluationTypeStatistic(
		type = type,
		earned = earned,
		evaluated = evaluated,
		gradedCount = graded,
		totalCount = total
	)
}

// The term in progress as the hub reads it, with its subjects and the points each one has evaluated so far.
fun progressTermOf(vararg evaluatedPerSubject: Double?): SummaryTerm.InProgress {
	return SummaryTerm.InProgress(
		periodCode = AcademicTermPeriod.SEP_DEC,
		periodYear = CURRENT_YEAR,
		academicWeek = 3,
		subjects = evaluatedPerSubject.mapIndexed { index, evaluated ->
			SummarySubjectPoints(
				subjectCode = "SUBJECT-$index",
				points = evaluated?.let { points ->
					EvaluationPoints(earned = 0.0, evaluated = points, gradedCount = 0, totalCount = 1)
				},
				attemptId = "attempt-$index",
				credits = 3
			)
		},
		hasSyncedEvaluations = true
	)
}

// The hub with the statistics it is given, the term in progress and the attention it says.
fun statisticsHubOf(
	statistics: SummaryStatistics? = statisticsOf(),
	term: SummaryTerm? = null,
	attention: List<SummaryAttention> = emptyList()
): SummaryHub {
	return SummaryHub(
		attention = attention,
		week = null,
		term = term,
		career = null,
		statistics = statistics
	)
}

fun ungradedAttentionOf(count: Int, oldestEvaluationId: String = "evaluation-1"): SummaryAttention.UngradedEvaluations {
	return SummaryAttention.UngradedEvaluations(
		count = count,
		oldestWeekStart = LocalDate(OVERDUE_YEAR, OVERDUE_MONTH, OVERDUE_DAY),
		oldestEvaluationId = oldestEvaluationId
	)
}

private const val MAX_GRADE = 5
private const val APPROVED_SUBJECTS = 5
private const val ORDER_YEAR_FACTOR = 10
private const val CURRENT_YEAR = 2026
private const val OVERDUE_YEAR = 2026
private const val OVERDUE_MONTH = 10
private const val OVERDUE_DAY = 12
