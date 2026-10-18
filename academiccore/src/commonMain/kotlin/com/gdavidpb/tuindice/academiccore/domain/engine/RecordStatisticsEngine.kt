package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical

/**
 * The statistics of a record's closed terms, read from its academic projection. Current and synthetic terms are left
 * out of every one of them, and the indexes are the averages the projection already shows, never recomputed here.
 */
object RecordStatisticsEngine {
	/**
	 * [projection] must be the academic projection, `RecordProjectionEngine.projectAcademic`, so that the outcomes are the
	 * ones the record states. A caller that also shows the projection of the record shares that one projection rather
	 * than projecting twice. The terms come back in chronological order, whatever order the projection holds them in.
	 */
	fun statisticsOf(projection: RecordProjection): RecordStatistics {
		val closedTerms = projection.terms
			.filter { term -> term.kind.isHistorical }
			.sortedWith(compareBy(TermProjection::termOrder, TermProjection::id))
		val closedAttempts = closedTerms.flatMap { term -> term.attempts }
		val standings = closedAttempts
			.groupBy { attempt -> attempt.subjectCode }
			.values
			.map { history -> standingOf(history) }

		return RecordStatistics(
			terms = closedTerms.map { term -> termStatisticOf(term) },
			subjects = SubjectOutcomes(
				approved = standings.count { standing -> standing.state == SubjectState.APPROVED },
				failed = standings.count { standing -> standing.state == SubjectState.FAILED },
				withdrawn = standings.count { standing -> standing.state == SubjectState.WITHDRAWN },
				noResult = standings.count { standing -> standing.state == SubjectState.NO_RESULT },
				approvedFirstTry = standings.count { standing ->
					standing.state == SubjectState.APPROVED && standing.firstResultApproved
				},
				firstTryBase = standings.count { standing -> standing.resultCount > 0 },
				repeated = standings.count { standing -> standing.resultCount > 1 }
			),
			grades = gradesOf(closedAttempts)
		)
	}

	/**
	 * The statistic of one term, as the closed terms are counted: its index, its cumulative index and the credits and
	 * subjects it approved. It reads a term in progress the same way, which is how a term report reads its own term.
	 */
	fun termStatisticOf(term: TermProjection): TermStatistic {
		val approvals = term.attempts
			.filter { attempt -> isApprovalResult(attempt) }
			.distinctBy { attempt -> attempt.subjectCode }

		return TermStatistic(
			termKey = term.termKey,
			periodCode = term.periodCode,
			periodYear = term.periodYear,
			termOrder = term.termOrder,
			kind = term.kind,
			index = averageToShow(term.periodAverage),
			cumulativeIndex = averageToShow(term.cumulativeAverage),
			approvedCredits = approvals.sumOf { attempt -> attempt.credits },
			approvedSubjects = approvals.size,
			isRegular = term.periodCode != AcademicTermPeriod.JUL_AUG
		)
	}

	// The projection answers 0.0 when there is no grade to average, and 0.0 is no average to show.
	private fun averageToShow(average: Double): Double? = average.takeIf { value -> value != 0.0 }

	// The history holds the attempts of one subject in chronological order. The state is decided by the first result
	// that settles it.
	private fun standingOf(history: List<AttemptProjection>): SubjectStanding {
		val results = history.filter { attempt -> isResult(attempt) }
		val approval = results.firstOrNull { attempt -> attempt.outcome == AttemptOutcome.APPROVED }
		val withdrawal = history.firstOrNull { attempt -> isRetired(attempt) }
		val state = when {
			approval != null -> SubjectState.APPROVED
			results.isNotEmpty() -> SubjectState.FAILED
			withdrawal != null -> SubjectState.WITHDRAWN
			else -> SubjectState.NO_RESULT
		}

		return SubjectStanding(
			state = state,
			resultCount = results.size,
			firstResultApproved = results.firstOrNull()?.outcome == AttemptOutcome.APPROVED
		)
	}

	private fun gradesOf(attempts: List<AttemptProjection>): GradeHistogram {
		val gradeCounts = attempts
			.filter { attempt -> attempt.gradingMode == AttemptGradingMode.NUMERIC && isResult(attempt) }
			.mapNotNull { attempt -> attempt.score.numericValue }
			.groupingBy { grade -> grade }
			.eachCount()

		// A grade of zero is no grade, so the histogram starts at the first grade a result can carry.
		return GradeHistogram(
			countsByGrade = (1..MAX_SUBJECT_GRADE).associateWith { grade -> gradeCounts[grade] ?: 0 },
			unreported = attempts.count { attempt ->
				attempt.outcome == AttemptOutcome.UNREPORTED && !isRetired(attempt)
			}
		)
	}

	// A result is what the retake timeline counts. A retired attempt never is, whatever grade it carries.
	private fun isResult(attempt: AttemptProjection): Boolean {
		return !isRetired(attempt) && RecordProjectionEngine.countsTowardRetakeTimeline(
			attempt.gradingMode,
			attempt.score,
			attempt.outcome
		)
	}

	private fun isApprovalResult(attempt: AttemptProjection): Boolean {
		return isResult(attempt) && attempt.outcome == AttemptOutcome.APPROVED
	}

	private fun isRetired(attempt: AttemptProjection): Boolean {
		return attempt.withdrawn || attempt.outcome == AttemptOutcome.RETIRED
	}

	private enum class SubjectState {
		APPROVED,
		FAILED,
		WITHDRAWN,
		NO_RESULT
	}

	private data class SubjectStanding(
		val state: SubjectState,
		val resultCount: Int,
		val firstResultApproved: Boolean
	)
}
