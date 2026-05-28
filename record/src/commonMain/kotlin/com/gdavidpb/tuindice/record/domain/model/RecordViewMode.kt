package com.gdavidpb.tuindice.record.domain.model

enum class RecordViewMode(
	val storageValue: String
) {
	Historical("historical"),
	Projection("projection");

	companion object {
		fun fromStorageValue(value: String?): RecordViewMode {
			return entries.firstOrNull { mode -> mode.storageValue == value }
				?: Projection
		}
	}
}
