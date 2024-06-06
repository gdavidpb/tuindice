package com.gdavidpb.tuindice.evaluations.domain.exception

import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError

class AddEvaluationIllegalArgumentException(
	val error: AddEvaluationUseCaseError
) : IllegalArgumentException()