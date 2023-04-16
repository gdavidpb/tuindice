package com.gdavidpb.tuindice.evaluations.domain.exception

import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError

class EvaluationIllegalArgumentException(
	val error: EvaluationError
) : IllegalArgumentException()