package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isOfficialHistorical

fun List<TermProjection>.filterByViewMode(viewMode: RecordViewMode): List<TermProjection> {
	return filter { term ->
		when (viewMode) {
			RecordViewMode.Official -> term.kind.isOfficialHistorical
			RecordViewMode.Working -> true
		}
	}
}
