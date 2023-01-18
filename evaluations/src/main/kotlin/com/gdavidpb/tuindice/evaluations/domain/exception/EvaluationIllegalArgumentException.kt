package com.gdavidpb.tuindice.evaluations.domain.exception

import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError

class EvaluationIllegalArgumentException(
	val error: EvaluationError
) : IllegalArgumentException()