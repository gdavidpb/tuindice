package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm

// The last term that is over, read the way Registro reads its terms: the projection of the record without its planned
// terms, the historical ones, the highest order first. Its index is the period average Registro shows, and its approved
// credits are those of the attempts the projection resolved as approved, the withdrawn ones left out.
internal fun finishedTermOf(record: AcademicRecord): SummaryFinishedTerm? {
	val finished = RecordProjectionEngine.projectAcademic(record).terms
		.filter { projected -> projected.kind.isHistorical }
		.maxWithOrNull(compareBy<TermProjection>({ projected -> projected.termOrder }, { projected -> projected.id }))
		?: return null

	return SummaryFinishedTerm(
		periodCode = finished.periodCode,
		periodYear = finished.periodYear,
		index = finished.periodAverage,
		approvedCredits = finished.attempts
			.filter { attempt -> !attempt.withdrawn && attempt.outcome == AttemptOutcome.APPROVED }
			.sumOf(AttemptProjection::credits)
	)
}
