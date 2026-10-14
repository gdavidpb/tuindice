package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm

// The last term that is over, read the way Registro reads its terms: the historical terms of the projection of the
// record, the highest order first. Its index is the period average Registro shows, and its approved credits are those
// of the attempts the projection resolved as approved, the withdrawn ones left out. The projection is the one the
// record is read through once, so the closed-term statistics are read from the same one.
internal fun finishedTermOf(projection: RecordProjection): SummaryFinishedTerm? {
	val finished = projection.terms
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
