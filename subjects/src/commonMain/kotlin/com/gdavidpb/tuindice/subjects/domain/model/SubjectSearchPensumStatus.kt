package com.gdavidpb.tuindice.subjects.domain.model

// What the student's pensum says about a search result. The first four mirror a fixed course's
// status; the last two mean it is not a fixed course but an open slot of that family would take it.
enum class SubjectSearchPensumStatus {
	APPROVED,
	CURRENT,
	AVAILABLE,
	BLOCKED,
	COUNTS_AS_ELECTIVE,
	COUNTS_AS_GENERAL_STUDIES
}
