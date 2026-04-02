package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter

fun List<Quarter>.filterByViewMode(viewMode: RecordViewMode): List<Quarter> {
	return filter { quarter ->
		when (viewMode) {
			RecordViewMode.Official -> quarter.isReadOnly
			RecordViewMode.Simulation -> true
		}
	}
}

fun List<Quarter>.containsQuarterInViewMode(
	viewMode: RecordViewMode,
	quarterId: String?
): Boolean {
	return (quarterId != null) && any { quarter ->
		quarter.id == quarterId &&
			when (viewMode) {
				RecordViewMode.Official -> quarter.isReadOnly
				RecordViewMode.Simulation -> true
			}
	}
}

fun RecordViewMode.other(): RecordViewMode {
	return when (this) {
		RecordViewMode.Official -> RecordViewMode.Simulation
		RecordViewMode.Simulation -> RecordViewMode.Official
	}
}
