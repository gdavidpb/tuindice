package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicAttentionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttention
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

// The points are the academic engine's; the summary only says them in its own model. The dates of the
// evaluations are read in the device's zone, the one the summary has always read them in.
internal fun attentionOf(
	term: AcademicTerm,
	evaluations: List<Evaluation>,
	today: LocalDate
): List<SummaryAttention> {
	return AcademicAttentionEngine
		.attentionOf(term = term, evaluations = evaluations, today = today, timeZone = TimeZone.currentSystemDefault())
		.map { attention -> attention.toSummaryAttention() }
}

private fun AcademicAttention.toSummaryAttention(): SummaryAttention {
	return when (this) {
		is AcademicAttention.UngradedEvaluations -> SummaryAttention.UngradedEvaluations(
			count = count,
			oldestWeekStart = oldestWeekStart,
			oldestEvaluationId = oldestEvaluationId
		)

		is AcademicAttention.EnrollmentError -> SummaryAttention.EnrollmentError(
			subjectCode = subjectCode,
			error = error,
			attemptId = attemptId
		)
	}
}
