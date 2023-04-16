package com.gdavidpb.tuindice.record.domain.usecase.error

sealed class SubjectError {
	object OutOfRangeGrade : SubjectError()
}