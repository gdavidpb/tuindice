package com.gdavidpb.tuindice.record.domain.exception

import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError

class SubjectIllegalArgumentException(
	val error: SubjectUseCaseError
) : IllegalArgumentException()