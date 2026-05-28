package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical

fun List<TermProjection>.filterByViewMode(viewMode: RecordViewMode): List<TermProjection> {
	return filter { term ->
		when (viewMode) {
			RecordViewMode.Historical -> term.kind.isHistorical
			RecordViewMode.Projection -> true
		}
	}
}

fun RecordViewMode.other(): RecordViewMode {
	return when (this) {
		RecordViewMode.Historical -> RecordViewMode.Projection
		RecordViewMode.Projection -> RecordViewMode.Historical
	}
}

fun AcademicRecord.projectionFor(viewMode: RecordViewMode): RecordProjection {
	return when (viewMode) {
		RecordViewMode.Historical -> RecordProjectionEngine.projectAcademic(this)
		RecordViewMode.Projection -> RecordProjectionEngine.projectProjection(this)
	}
}

fun AcademicRecord.filteredProjectionFor(viewMode: RecordViewMode): RecordProjection {
	val projection = projectionFor(viewMode)
	return projection.copy(
		terms = projection.terms.filterByViewMode(viewMode)
	)
}
