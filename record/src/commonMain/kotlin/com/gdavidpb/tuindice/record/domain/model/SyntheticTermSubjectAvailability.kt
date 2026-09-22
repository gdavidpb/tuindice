package com.gdavidpb.tuindice.record.domain.model

enum class SyntheticTermSubjectAvailability {
	APPROVED,
	CURRENT,
	AVAILABLE,
	BLOCKED,
	ALREADY_PLANNED,
	NOT_IN_PENSUM,

	// Not a fixed course, but an open elective or Estudios Generales slot of the pensum takes it.
	COUNTS_AS_SLOT,
}
