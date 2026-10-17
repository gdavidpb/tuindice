package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

/**
 * "Trimestre": the term in progress, or, when none is in progress, what the record says of the terms it has.
 */
sealed interface SummaryTerm {
	/**
	 * The term in progress: its period, the academic week it is (null outside weeks 1 to 12) and the points of each of
	 * its subjects. The points are empty while the evaluations are not downloaded ([hasSyncedEvaluations] false),
	 * because saying a subject has no grades would not be known.
	 *
	 * [termId] is the record's id of the term, the one the Record screen selects.
	 */
	data class InProgress(
		val periodCode: AcademicTermPeriod,
		val periodYear: Int,
		val academicWeek: Int?,
		val subjects: List<SummarySubjectPoints>,
		val hasSyncedEvaluations: Boolean,
		val termId: String? = null
	) : SummaryTerm {
		/** The weeks the term has after the one it is in, out of its twelve; null outside weeks 1 to 12. */
		val weeksLeft: Int?
			get() = academicWeek?.let { week -> AcademicWeekEngine.MAX_ACADEMIC_WEEK - week }

		/** The credits of the subjects the term lists, the ones its rows show. */
		val credits: Int
			get() = subjects.sumOf(SummarySubjectPoints::credits)
	}

	/**
	 * No term is in progress. [hasTerms] says whether the record has any term at all (a new student has none), and
	 * [finished] is the last term that is over, if there is one.
	 */
	data class Idle(
		val hasTerms: Boolean,
		val finished: SummaryFinishedTerm?
	) : SummaryTerm
}
