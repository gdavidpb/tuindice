package com.gdavidpb.tuindice.evaluations.domain.exception

import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError

class AddEvaluationIllegalArgumentException(
	val error: AddEvaluationError
) : IllegalArgumentException()