package com.gdavidpb.tuindice.record.domain.error

sealed class SubjectError {
	object OutOfRangeGrade : SubjectError()
}