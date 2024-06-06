package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed class SubjectUseCaseError : UseCaseError {
	data object OutOfRangeGrade : SubjectUseCaseError()
}