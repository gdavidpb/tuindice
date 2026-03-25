package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface SubjectUseCaseError : UseCaseError {
	data object NotFound : SubjectUseCaseError
	data object Conflict : SubjectUseCaseError
	data object ReadOnly : SubjectUseCaseError
	data object OutOfRangeGrade : SubjectUseCaseError
}
