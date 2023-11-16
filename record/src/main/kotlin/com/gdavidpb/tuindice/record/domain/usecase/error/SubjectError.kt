package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class SubjectError : Error {
	data object OutOfRangeGrade : SubjectError()
}