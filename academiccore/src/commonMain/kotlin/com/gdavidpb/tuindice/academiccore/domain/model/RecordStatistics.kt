package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * What the closed terms of a record say, as `RecordStatisticsEngine.statisticsOf` reads them: the term indexes in
 * chronological order, and the outcomes of the subjects and the grades, taken over all of those terms.
 */
data class RecordStatistics(
	val terms: List<TermStatistic>,
	val subjects: SubjectOutcomes,
	val grades: GradeHistogram
)
