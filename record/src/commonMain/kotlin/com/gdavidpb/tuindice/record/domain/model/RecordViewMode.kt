package com.gdavidpb.tuindice.record.domain.model

enum class RecordViewMode(
	val storageValue: String
) {
	Official("official"),
	Working("working");

	companion object {
		fun fromStorageValue(value: String?): RecordViewMode {
			return entries.firstOrNull { mode -> mode.storageValue == value }
				?: Working
		}
	}
}
