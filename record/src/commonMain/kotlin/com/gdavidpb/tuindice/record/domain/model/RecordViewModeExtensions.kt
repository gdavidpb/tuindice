package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isOfficialCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isOfficialHistorical

fun List<TermProjection>.filterByViewMode(viewMode: RecordViewMode): List<TermProjection> {
	return filter { term ->
		when (viewMode) {
			RecordViewMode.Official -> term.kind.isOfficialHistorical || term.kind.isOfficialCurrent
			RecordViewMode.Working -> true
		}
	}
}

fun RecordViewMode.other(): RecordViewMode {
	return when (this) {
		RecordViewMode.Official -> RecordViewMode.Working
		RecordViewMode.Working -> RecordViewMode.Official
	}
}

fun AcademicRecord.projectionFor(viewMode: RecordViewMode): RecordProjection {
	return when (viewMode) {
		RecordViewMode.Official -> RecordProjectionEngine.projectOfficial(this)
		RecordViewMode.Working -> RecordProjectionEngine.projectWorking(this)
	}
}

fun AcademicRecord.filteredProjectionFor(viewMode: RecordViewMode): RecordProjection {
	val projection = projectionFor(viewMode)
	return projection.copy(
		terms = projection.terms.filterByViewMode(viewMode)
	)
}
